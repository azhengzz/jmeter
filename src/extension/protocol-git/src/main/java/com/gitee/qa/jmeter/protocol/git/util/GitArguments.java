package com.gitee.qa.jmeter.protocol.git.util;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.PropertyIterator;

import java.util.LinkedHashMap;
import java.util.Map;

public class GitArguments extends Arguments {

    public GitArguments(){
        super();
    }

    public Map<String, String[]> getGitArgumentsAsMap() {
        PropertyIterator iter = getArguments().iterator();
        Map<String, String[]> argMap = new LinkedHashMap<>();

        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), new String[]{arg.getValue(), arg.getDescription()});
            }
        }
        return argMap;
    }

    public GitArguments getGitCloneDefaultParameters() {
        GitArguments params = new GitArguments();
        params.addArgument(CloneParameters.BRANCH, "", null, CloneParameters.BRANCH_DESC);
//        params.addArgument("--depth", "", null, "Create a shallow clone with a history truncated to the specified number of commits.");  // jgit暂不支持shallow clone
        params.addArgument(CloneParameters.REPOSITORY, "", null, CloneParameters.REPOSITORY_DESC);
        params.addArgument(CloneParameters.DIRECTORY, "", null, CloneParameters.DIRECTORY_DESC);
        return params;
    }

    public GitArguments getGitCommitDefaultParameters() {
        GitArguments params = new GitArguments();
        params.addArgument(CommitParameters.REPO_PATH, "", null, CommitParameters.REPO_PATH_DESC);
        params.addArgument(CommitParameters.MESSAGE, "", null, CommitParameters.MESSAGE_DESC);
        params.addArgument(CommitParameters.AUTHOR_NAME, "", null, CommitParameters.AUTHOR_NAME_DESC);
        params.addArgument(CommitParameters.AUTHOR_EMAIL, "", null, CommitParameters.AUTHOR_EMAIL_DESC);
        return params;
    }

    public GitArguments getGitAddDefaultParameters() {
        GitArguments params = new GitArguments();
        params.addArgument(AddParameters.REPO_PATH, "", null, AddParameters.REPO_PATH_DESC);
        params.addArgument(AddParameters.PATHSPEC, "", null, AddParameters.PATHSPEC_DESC);
        return params;
    }

    public GitArguments getGitPushDefaultParameters() {
        GitArguments params = new GitArguments();
        params.addArgument(PushParameters.REPO_PATH, "", null, PushParameters.REPO_PATH_DESC);
        params.addArgument(PushParameters.FORCE, "", null, PushParameters.FORCE_DESC);
        params.addArgument(PushParameters.REF_SPEC, "", null, PushParameters.REF_SPEC_DESC);
        return params;
    }

    public GitArguments getGitPullDefaultParameters() {
        GitArguments params = new GitArguments();
        params.addArgument(PullParameters.REPO_PATH, "", null, PullParameters.REPO_PATH_DESC);
        return params;
    }

    public GitArguments getGitBranchDefaultParameters() {
        GitArguments params = new GitArguments();
        params.addArgument(BranchParameters.REPO_PATH, "", null, BranchParameters.REPO_PATH_DESC);
        params.addArgument(BranchParameters.ACTION, "", null, BranchParameters.ACTION_DESC);
        params.addArgument(BranchParameters.NAME, "", null, BranchParameters.NAME_DESC);
        return params;
    }

}
