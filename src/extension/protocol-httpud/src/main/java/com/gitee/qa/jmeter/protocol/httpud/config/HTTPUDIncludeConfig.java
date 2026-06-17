package com.gitee.qa.jmeter.protocol.httpud.config;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

public class HTTPUDIncludeConfig extends ConfigTestElement implements ReplaceableController {
    private static final Logger log = LoggerFactory.getLogger(HTTPUDIncludeConfig.class);

    private static final String INCLUDE_PATH = "HTTPUDIncludeConfig.includepath"; //$NON-NLS-1$

    private static final String PREFIX =
            JMeterUtils.getPropDefault(
                    "includecontroller.prefix", //$NON-NLS-1$
                    ""); //$NON-NLS-1$

    private HashTree subtree = null;
    private TestElement sub = null;
    // 记录最近一次加载的 jmx 文件的信息
    private String lastFileName = "";
    private long lastFileModifiedTime;
    private long lastFileSize;

    public String getIncludePath() {
        return this.getPropertyAsString(INCLUDE_PATH);
    }

    public void setIncludePath(String jmxfile) {
        this.setProperty(INCLUDE_PATH, jmxfile);
    }

    public void resolveReplacementSubTree(JMeterTreeNode context) {
        this.subtree = this.loadIncludedElements();
    }

    @Override
    public Object clone() {
        // TODO - fix so that this is only called once per test, instead of at every clone
        // Perhaps save previous filename, and only load if it has changed?
        if (!this.isEnabled()) return super.clone();
        this.resolveReplacementSubTree(null);
        HTTPUDIncludeConfig clone = (HTTPUDIncludeConfig) super.clone();
        clone.setIncludePath(this.getIncludePath());
        // 文件元信息（lastFileName/lastFileModifiedTime/lastFileSize）是 mutable 字段，
        // super.clone() 只是浅拷贝引用；不重置会导致多线程下克隆体共享同一份缓存，
        // 出现「某线程刚加载完，另一线程误以为文件未修改而跳过加载」的问题。
        clone.resetFileInfo();
        if (this.subtree != null) {
            if (this.subtree.size() == 1) {
                for (Object o : this.subtree.keySet()) {
                    this.sub = (TestElement) o;
                }
            }
            clone.subtree = (HashTree)this.subtree.clone();
            clone.sub = this.sub==null ? null : (TestElement) this.sub.clone();
        }
        return clone;
    }

    /**
     * The way ReplaceableController works is clone is called first,
     * followed by replace(HashTree) and finally getReplacement().
     */
    public HashTree getReplacementSubTree() {
        return subtree;
    }

    /**
     * load the included elements using SaveService
     * (代码 Copy 自 org.apache.jmeter.control.IncludeController)
     *
     * @return tree with loaded elements
     */
    protected HashTree loadIncludedElements() {
        // only try to load the JMX test plan if there is one
        final String includePath = getIncludePath();
        HashTree tree = null;
        if (includePath != null && includePath.length() > 0) {
            String fileName = PREFIX + includePath;
            try {
                File file = new File(fileName.trim());
                final String absolutePath = file.getAbsolutePath();
                log.info("loadIncludedElements -- try to load included module: {}", absolutePath);
                if (!file.exists() && !file.isAbsolute()) {
                    log.info("loadIncludedElements -failed for: {}", absolutePath);
                    file = new File(FileServer.getFileServer().getBaseDir(), includePath);
                    if (log.isInfoEnabled()) {
                        log.info("loadIncludedElements -Attempting to read it from: {}", file.getAbsolutePath());
                    }
                    if (!file.canRead() || !file.isFile()) {
                        log.error("Include Controller '{}' can't load '{}' - see log for details", this.getName(),
                                fileName);
                        this.resetFileInfo();
                        throw new IOException("loadIncludedElements -failed for: " + absolutePath +
                                " and " + file.getAbsolutePath());
                    }
                }
                // 检测下文件的更新时间，如果未更新则不进行 load
                if (!jmxIsModified(file)) {
                    return this.subtree;
                }
                tree = SaveService.loadTree(file);
                // filter the tree for a TestFragment.
                tree = getProperBranch(tree);
                removeDisabledItems(tree);
                return tree;
            } catch (NoClassDefFoundError ex) // Allow for missing optional jars
            {
                String msg = "Including file \"" + fileName
                        + "\" failed for Include Controller \"" + this.getName()
                        + "\", missing jar file";
                log.warn(msg, ex);
                JMeterUtils.reportErrorToUser(msg + " - see log for details");
            } catch (FileNotFoundException ex) {
                String msg = "File \"" + fileName
                        + "\" not found for Include Controller \"" + this.getName() + "\"";
                JMeterUtils.reportErrorToUser(msg + " - see log for details");
                log.warn(msg, ex);
            } catch (Exception ex) {
                String msg = "Including file \"" + fileName
                        + "\" failed for Include Controller \"" + this.getName()
                        + "\", unexpected error";
                JMeterUtils.reportErrorToUser(msg + " - see log for details");
                log.warn(msg, ex);
            }
        }
        return tree;
    }

    /**
     * Extract from tree (included test plan) all Test Elements located in a Test Fragment
     *
     * @param tree HashTree included Test Plan
     * @return HashTree Subset within Test Fragment or Empty HashTree
     */
    private HashTree getProperBranch(HashTree tree) {
        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;

            //if we found a TestPlan, then we are on our way to the TestFragment
            if (item instanceof TestPlan) {
                return getProperBranch(tree.getTree(item));
            }

            if (item instanceof TestFragmentController) {
                return tree.getTree(item);
            }
        }
        log.warn("No Test Fragment was found in included Test Plan, returning empty HashTree");
        return new HashTree();
    }

    private void removeDisabledItems(HashTree tree) {
        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;
            if (!item.isEnabled()) {
                tree.remove(item);
            } else {
                removeDisabledItems(tree.getTree(item));// Recursive call
            }
        }
    }

    /**
     * 重置 include 文件状态
     * */
    private void resetFileInfo() {
        this.lastFileName = "";
        this.lastFileSize = 0;
        this.lastFileModifiedTime = 0;
    }

    /**
     * 检查jmx文件是否已更新
     *
     * */
    private boolean jmxIsModified(File file) {
        String fileName = file.getAbsolutePath();
        long fileModifiedTime = file.lastModified();
        long fileSize = file.length();
        if (this.lastFileName.equals(fileName) && this.lastFileModifiedTime == fileModifiedTime && this.lastFileSize == fileSize) {
            return false;
        }else {
            this.lastFileName = fileName;
            this.lastFileModifiedTime = fileModifiedTime;
            this.lastFileSize = fileSize;
            return true;
        }
    }
}
