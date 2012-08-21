package uk.ac.susx.mlcl.lib.events;


public class ReportingProgressListener implements ProgressListener {

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        System.out.println(progressEvent.getSource().getProgressReport());
    }

}