/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

/**
 *
 * @author uha
 */
public class AnswerFromPublishModelDialog {
    public boolean publish;
    public boolean PublishAsNewModel;
    public boolean ForceCommit;
    public String serverURI;
    public String comment;
    public String folder;
    public String title;


    public AnswerFromPublishModelDialog(boolean publish, boolean PublishAsNewModel, boolean ForceCommit, String serverURI, String comment, String folder, String title) {
        this.publish = publish;
        this.PublishAsNewModel = PublishAsNewModel;
        this.ForceCommit = ForceCommit;
        this.serverURI = serverURI;
        this.comment = comment;
        this.folder = folder;
        this.title = title;
    }

}