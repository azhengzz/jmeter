package com.gitee.qa.jmeter.protocol.git;

import com.gitee.qa.jmeter.protocol.git.util.*;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class GitSampler extends AbstractSampler {

    private static final long serialVersionUID = 241L;

    private static final Logger LOG = LoggerFactory.getLogger(GitSampler.class);

    public static final String USE_SSH_PROTOCOL = "GitSampler.use_ssh_protocol"; // $NON-NLS-1$

    public static final String USE_HTTP_PROTOCOL = "GitSampler.use_http_protocol"; // $NON-NLS-1$

    public static final String SSH_KEY_PATH = "GitSampler.ssh_key_path"; // $NON-NLS-1$

    public static final String USER_NAME = "GitSampler.user_name"; // $NON-NLS-1$

    public static final String USER_PASSWORD = "GitSampler.user_password"; // $NON-NLS-1$

    public static final String ACTION = "GitSampler.action"; // $NON-NLS-1$

    public static final String ARGUMENTS = "GitSampler.Arguments"; // $NON-NLS-1$


    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());

        switch (getAction()) {
            case GitAction.CLONE:
                res = cloneSample(res);
                break;
            case GitAction.ADD:
                res = addSample(res);
                break;
            case GitAction.COMMIT:
                res = commitSample(res);
                break;
            case GitAction.PUSH:
                res = pushSample(res);
                break;
            case GitAction.PULL:
                res = pullSample(res);
                break;
            case GitAction.BRANCH:
                res = branchSample(res);
                break;
            default:
                res.setSuccessful(false);
                res.setResponseData(String.format("参数: %s 有误", ACTION), "utf-8");
                return res;
        }

        return res;
    }

    public SampleResult cloneSample(SampleResult res) {
        String userName = getUserName().trim();
        String password = getUserPassword().trim();

        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(userName, password);
        TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback();

        res.setSamplerData(
            CloneParameters.BRANCH + ": " + getGitArguments().get(CloneParameters.BRANCH) + "\n" +
            CloneParameters.REPOSITORY + ": " + getGitArguments().get(CloneParameters.REPOSITORY) + "\n" +
            CloneParameters.DIRECTORY + ": " + getGitArguments().get(CloneParameters.DIRECTORY) + "\n"
        );
        res.setRequestHeaders(
            "ssh" + ": " + this.useSSHProtocol() + "\n" +
            "http" + ": " + this.useHTTPProtocol() + "\n" +
            "id_rsa_path" + ": " + this.getSshKeyPath() + "\n" +
            "username" + ": " + this.getUserName() + "\n" +
            "password" + ": " + this.getUserPassword() + "\n"
        );

        res.sampleStart();
        Git git = null;
        try {
            CloneCommand cloneCommand = Git.cloneRepository();
            if (useHTTPProtocol()) {
                cloneCommand.setCredentialsProvider(provider);
            } else if (useSSHProtocol()) {
                cloneCommand.setTransportConfigCallback(transportConfigCallback);
            }
            if (getGitArguments().get(CloneParameters.BRANCH) != null && !getGitArguments().get(CloneParameters.BRANCH).trim().equals("")) {
                cloneCommand.setBranch(getGitArguments().get(CloneParameters.BRANCH));
            }
            git = cloneCommand.setURI(getGitArguments().get(CloneParameters.REPOSITORY))
                    .setDirectory(new File(getGitArguments().get(CloneParameters.DIRECTORY)))
                    .call();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            res.setSuccessful(false);
            res.setResponseData(e.getLocalizedMessage(), "utf-8");
            return res;
        } finally {
            if (git != null) {
                git.close();
            }
            res.sampleEnd();
        }
        // 取样器结果
        res.setResponseCodeOK();
        res.setResponseMessage("clone successfully.");
        res.setResponseData("clone successfully.", StandardCharsets.UTF_8.name());
        res.setResponseHeaders("");
        // Scrool显示区域
        res.setSuccessful(true);
        return res;
    }

    public SampleResult addSample(SampleResult res) {
        String repoPath;
        String pathSpec;

        res.sampleStart();

        if (getGitArguments().get(AddParameters.REPO_PATH) != null) {
            repoPath = getGitArguments().get(AddParameters.REPO_PATH).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", AddParameters.REPO_PATH), "utf-8");
            return res;
        }
        if (getGitArguments().get(AddParameters.PATHSPEC) != null) {
            pathSpec = getGitArguments().get(AddParameters.PATHSPEC).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", AddParameters.PATHSPEC), "utf-8");
            return res;
        }

        res.setRequestHeaders(
            "ssh" + ": " + this.useSSHProtocol() + "\n" +
            "http" + ": " + this.useHTTPProtocol() + "\n" +
            "id_rsa_path" + ": " + this.getSshKeyPath() + "\n" +
            "username" + ": " + this.getUserName() + "\n" +
            "password" + ": " + this.getUserPassword() + "\n"
        );
        res.setSamplerData(
            AddParameters.REPO_PATH + ": " + repoPath + "\n" +
            AddParameters.PATHSPEC + ": " + pathSpec + "\n"
        );

        try (Git git = Git.open(new File(repoPath))
        ) {
            git.add()
                    .addFilepattern(pathSpec)
                    .call();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            res.setSuccessful(false);
            res.setResponseData(e.getLocalizedMessage(), "utf-8");
            return res;
        } finally {
            res.sampleEnd();
        }
        // 取样器结果
        res.setResponseCodeOK();
        res.setResponseMessage("add successfully.");
        res.setResponseData("add successfully.", StandardCharsets.UTF_8.name());
        res.setResponseHeaders("");
        // Scrool显示区域
        res.setSuccessful(true);
        return res;
    }

    public SampleResult commitSample(SampleResult res) {
        String repoPath;
        String msg;
        String name = "";
        String email = "";

        res.sampleStart();

        if (getGitArguments().get(CommitParameters.REPO_PATH) != null) {
            repoPath = getGitArguments().get(CommitParameters.REPO_PATH).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", CommitParameters.REPO_PATH), "utf-8");
            return res;
        }
        if (getGitArguments().get(CommitParameters.MESSAGE) != null) {
            msg = getGitArguments().get(CommitParameters.MESSAGE).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", CommitParameters.MESSAGE), "utf-8");
            return res;
        }
        if (getGitArguments().get(CommitParameters.AUTHOR_NAME) != null) {
            name = getGitArguments().get(CommitParameters.AUTHOR_NAME).trim();
        }
        if (getGitArguments().get(CommitParameters.AUTHOR_EMAIL) != null) {
            email = getGitArguments().get(CommitParameters.AUTHOR_EMAIL).trim();
        }

        res.setRequestHeaders(
            "ssh" + ": " + this.useSSHProtocol() + "\n" +
            "http" + ": " + this.useHTTPProtocol() + "\n" +
            "id_rsa_path" + ": " + this.getSshKeyPath() + "\n" +
            "username" + ": " + this.getUserName() + "\n" +
            "password" + ": " + this.getUserPassword() + "\n"
        );
        res.setSamplerData(
            CommitParameters.REPO_PATH + ": " + repoPath + "\n" +
            CommitParameters.MESSAGE + ": " + msg + "\n" +
            CommitParameters.AUTHOR_NAME + ": " + name + "\n" +
            CommitParameters.AUTHOR_EMAIL + ": " + email + "\n"
        );

        try (Git git = Git.open(new File(repoPath))
        ) {
            CommitCommand cc = git.commit();
            if (!name.equals("") && !email.equals("")){
                cc.setCommitter(name, email);
            }
            cc.setMessage(msg);
            cc.call();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            res.setSuccessful(false);
            res.setResponseData(e.getLocalizedMessage(), "utf-8");
            return res;
        } finally {
            res.sampleEnd();
        }
        // 取样器结果
        res.setResponseCodeOK();
        res.setResponseMessage("commit successfully.");

        res.setResponseData("commit successfully.", StandardCharsets.UTF_8.name());
        res.setResponseHeaders("");
        // Scrool显示区域
        res.setSuccessful(true);
        return res;
    }

    public SampleResult pushSample(SampleResult res) {
        String repoPath;
        boolean force;
        String remote_response_message;
        boolean pushFailure = false;
        String refSpec;
        RemoteRefUpdate.Status status = RemoteRefUpdate.Status.OK;  // remote push status

        String userName = getUserName().trim();
        String password = getUserPassword().trim();
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(userName, password);
        TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback();

        res.sampleStart();

        if (getGitArguments().get(PushParameters.REPO_PATH) != null) {
            repoPath = getGitArguments().get(PushParameters.REPO_PATH).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", PushParameters.REPO_PATH), "utf-8");
            return res;
        }
        if (getGitArguments().get(PushParameters.FORCE) != null) {
            force = "true".equals(getGitArguments().get(PushParameters.FORCE).trim());
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", PushParameters.FORCE), "utf-8");
            return res;
        }
        if (getGitArguments().get(PushParameters.REF_SPEC) != null) {
            refSpec = getGitArguments().get(PushParameters.REF_SPEC).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", PushParameters.REF_SPEC), "utf-8");
            return res;
        }

        res.setRequestHeaders(
            "ssh" + ": " + this.useSSHProtocol() + "\n" +
            "http" + ": " + this.useHTTPProtocol() + "\n" +
            "id_rsa_path" + ": " + this.getSshKeyPath() + "\n" +
            "username" + ": " + this.getUserName() + "\n" +
            "password" + ": " + this.getUserPassword() + "\n"
        );
        res.setSamplerData(
            PushParameters.REPO_PATH + ": " + repoPath + "\n" +
            PushParameters.FORCE + ": " + force + "\n" +
            PushParameters.REF_SPEC + ": " + refSpec + "\n"
        );

        try (Git git = Git.open(new File(repoPath))
        ) {
            PushCommand pushCommand = git.push();
            OutputStream out = new OutputStream() {
                private StringBuilder string = new StringBuilder();

                @Override
                public void write(int b) throws IOException {
                    System.out.println((char) b );
                    this.string.append((char) b );
                }

                //Netbeans IDE automatically overrides this toString()
                public String toString() {
                    return this.string.toString();
                }
            };
            pushCommand.setOutputStream(out);
            if (useHTTPProtocol()) {
                pushCommand.setCredentialsProvider(provider);
            } else if (useSSHProtocol()) {
                pushCommand.setTransportConfigCallback(transportConfigCallback);
            }
            pushCommand = pushCommand.setForce(force);
            if (!"".equals(refSpec)){
                pushCommand.setRefSpecs(new RefSpec(refSpec));
            }
            Iterable<PushResult> pushResults = pushCommand.call();
            OUT:
            for (PushResult pushResult : pushResults) {
                Collection<RemoteRefUpdate> remoteRefUpdates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate remoteRefUpdate : remoteRefUpdates) {
                    status = remoteRefUpdate.getStatus();
                    if (status == RemoteRefUpdate.Status.REJECTED_NODELETE ||
                        status == RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD ||
                        status == RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED ||
                        status == RemoteRefUpdate.Status.REJECTED_OTHER_REASON ||
                        status == RemoteRefUpdate.Status.AWAITING_REPORT ||
                        status == RemoteRefUpdate.Status.NON_EXISTING
                    ){
                        pushFailure = true;
                        break OUT;
                    }
                }
            }
            remote_response_message = out.toString();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            res.setSuccessful(false);
            res.setResponseData(e.getLocalizedMessage(), "utf-8");
            return res;
        } finally {
            res.sampleEnd();
        }
        // 取样器结果
        res.setResponseCodeOK();
        if (!pushFailure){
            res.setResponseMessage(String.format("push successfully.\nstatus: %s\nremote: %s", status, remote_response_message));
            res.setResponseData(String.format("push successfully.\nstatus: %s\nremote: %s", status, remote_response_message), StandardCharsets.UTF_8.name());
        } else {
            res.setResponseMessage(String.format("push failure.\nstatus: %s\nremote: %s", status, remote_response_message));
            res.setResponseData(String.format("push failure.\nstatus: %s\nremote: %s", status, remote_response_message), StandardCharsets.UTF_8.name());
        }

        res.setResponseHeaders("");
        // Scrool显示区域
        res.setSuccessful(true);
        return res;
    }

    public SampleResult pullSample(SampleResult res) {
        String repoPath;

        String userName = getUserName().trim();
        String password = getUserPassword().trim();
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(userName, password);
        TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback();

        res.sampleStart();

        if (getGitArguments().get(PullParameters.REPO_PATH) != null) {
            repoPath = getGitArguments().get(PullParameters.REPO_PATH).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", PullParameters.REPO_PATH), "utf-8");
            return res;
        }

        res.setRequestHeaders(
            "ssh" + ": " + this.useSSHProtocol() + "\n" +
            "http" + ": " + this.useHTTPProtocol() + "\n" +
            "id_rsa_path" + ": " + this.getSshKeyPath() + "\n" +
            "username" + ": " + this.getUserName() + "\n" +
            "password" + ": " + this.getUserPassword() + "\n"
        );
        res.setSamplerData(
            PullParameters.REPO_PATH + ": " + repoPath + "\n"
        );

        try (Git git = Git.open(new File(repoPath))
        ) {
            PullCommand pullCommand = git.pull();
            if (useHTTPProtocol()) {
                pullCommand.setCredentialsProvider(provider);
            } else if (useSSHProtocol()) {
                pullCommand.setTransportConfigCallback(transportConfigCallback);
            }
            pullCommand.call();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            res.setSuccessful(false);
            res.setResponseData(e.getLocalizedMessage(), "utf-8");
            return res;
        } finally {
            res.sampleEnd();
        }
        // 取样器结果
        res.setResponseCodeOK();
        res.setResponseMessage("pull successfully.");
        res.setResponseData("pull successfully.", StandardCharsets.UTF_8.name());
        res.setResponseHeaders("");
        // Scrool显示区域
        res.setSuccessful(true);
        return res;
    }

    public SampleResult branchSample(SampleResult res) {
        String repoPath;
        String action;
        String branchName;

        res.sampleStart();

        if (getGitArguments().get(BranchParameters.REPO_PATH) != null) {
            repoPath = getGitArguments().get(BranchParameters.REPO_PATH).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", BranchParameters.REPO_PATH), "utf-8");
            return res;
        }
        if (getGitArguments().get(BranchParameters.ACTION) != null) {
            action = getGitArguments().get(BranchParameters.ACTION).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", BranchParameters.ACTION), "utf-8");
            return res;
        }
        if (getGitArguments().get(BranchParameters.NAME) != null) {
            branchName = getGitArguments().get(BranchParameters.NAME).trim();
        }else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 不能为空", BranchParameters.NAME), "utf-8");
            return res;
        }

        res.setRequestHeaders(
                "ssh" + ": " + this.useSSHProtocol() + "\n" +
                "http" + ": " + this.useHTTPProtocol() + "\n" +
                "id_rsa_path" + ": " + this.getSshKeyPath() + "\n" +
                "username" + ": " + this.getUserName() + "\n" +
                "password" + ": " + this.getUserPassword() + "\n"
        );
        res.setSamplerData(
                BranchParameters.REPO_PATH + ": " + repoPath + "\n" +
                BranchParameters.ACTION + ": " + action + "\n" +
                BranchParameters.NAME + ": " + branchName + "\n"
        );

        try (Git git = Git.open(new File(repoPath))
        ) {
            // 分支创建
            if (BranchParameters.BranchAction.CREATE.equals(action)){
                // 分支创建需要分支名参数
                CreateBranchCommand createBranchCommand  = git.branchCreate();
                createBranchCommand.setName(branchName);
                createBranchCommand.call();
            }else {  // 当前不支持其他分支动作
                res.setSuccessful(false);
                res.setResponseData(String.format("参数: %s 的值为: %s，当前不支持该类型", BranchParameters.ACTION, action), "utf-8");
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
            res.setSuccessful(false);
            res.setResponseData(e.getLocalizedMessage(), "utf-8");
            return res;
        } finally {
            res.sampleEnd();
        }
        // 取样器结果
        res.setResponseCodeOK();
        res.setResponseMessage("git branch successfully.");
        res.setResponseData("git branch successfully.", StandardCharsets.UTF_8.name());
        res.setResponseHeaders("");
        // Scrool显示区域
        res.setSuccessful(true);
        return res;

    }

    private class SshTransportConfigCallback implements TransportConfigCallback {
        @Override
        public void configure(Transport transport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new SshSessionFactory() {
                @Override
                public RemoteSession getSession(URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms)
                        throws TransportException {
                    try {
                        JSch jsch = new JSch();
                        jsch.addIdentity(GitSampler.this.getSshKeyPath());
                        String host = uri.getHost();
                        int port = uri.getPort() > 0 ? uri.getPort() : 22;
                        String user = uri.getUser() != null ? uri.getUser() : "git";
                        final Session session = jsch.getSession(user, host, port);
                        session.setConfig("StrictHostKeyChecking", "no");
                        return new JschRemoteSession(session);
                    } catch (JSchException e) {
                        throw new TransportException(uri, e.getMessage());
                    }
                }

                @Override
                public String getType() {
                    return "jsch";
                }
            });
        }
    }

    private static class JschRemoteSession implements RemoteSession {
        private final Session session;

        JschRemoteSession(Session session) {
            this.session = session;
        }

        @Override
        public Process exec(String commandName, int timeout) throws IOException {
            try {
                session.connect(timeout);
                return new JschProcess(session, commandName);
            } catch (JSchException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void disconnect() {
            session.disconnect();
        }
    }

    private static class JschProcess extends Process {
        private final Session session;
        private final String commandName;

        JschProcess(Session session, String commandName) {
            this.session = session;
            this.commandName = commandName;
        }

        @Override
        public OutputStream getOutputStream() {
            return new OutputStream() {
                @Override
                public void write(int b) {}
            };
        }

        @Override
        public InputStream getInputStream() {
            try {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(commandName);
                return channel.getInputStream();
            } catch (JSchException e) {
                return new ByteArrayInputStream(new byte[0]);
            } catch (IOException e) {
                return new ByteArrayInputStream(new byte[0]);
            }
        }

        @Override
        public InputStream getErrorStream() {
            try {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(commandName);
                return channel.getErrStream();
            } catch (JSchException e) {
                return new ByteArrayInputStream(new byte[0]);
            } catch (IOException e) {
                return new ByteArrayInputStream(new byte[0]);
            }
        }

        @Override
        public int waitFor() { return 0; }

        @Override
        public int exitValue() { return 0; }

        @Override
        public void destroy() {
            session.disconnect();
        }
    }

    public boolean useSSHProtocol() {
        return getPropertyAsBoolean(USE_SSH_PROTOCOL);
    }

    public boolean useHTTPProtocol() {
        return getPropertyAsBoolean(USE_HTTP_PROTOCOL);
    }

    public String getSshKeyPath() {
        return getPropertyAsString(SSH_KEY_PATH);
    }

    public String getUserName() {
        return getPropertyAsString(USER_NAME);
    }

    public String getUserPassword() {
        return getPropertyAsString(USER_PASSWORD);
    }

    public String getAction() {
        return getPropertyAsString(ACTION);
    }

    public Map<String, String> getGitArguments() {
        GitArguments arguments = (GitArguments) getProperty(GitSampler.ARGUMENTS).getObjectValue();
        return arguments.getArgumentsAsMap();
    }
}
