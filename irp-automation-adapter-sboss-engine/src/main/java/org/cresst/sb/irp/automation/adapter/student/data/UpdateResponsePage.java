package org.cresst.sb.irp.automation.adapter.student.data;

public class UpdateResponsePage {
    private int pageNumber;
    private String groupId;
    private String pageKey;
    private int segmentPosition;
    private String segmentId;

    public UpdateResponsePage(String groupId, String pageKey, int pageNumber, int segmentPosition, String segmentId) {
        this.pageNumber = pageNumber;
        this.groupId = groupId;
        this.pageKey = pageKey;
        this.segmentPosition = segmentPosition;
        this.segmentId = segmentId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getPageKey() {
        return pageKey;
    }

    public void setPageKey(String pageKey) {
        this.pageKey = pageKey;
    }

    public int getSegmentPosition() {
        return segmentPosition;
    }

    public void setSegmentPosition(int segmentPosition) {
        this.segmentPosition = segmentPosition;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    @Override
    public String toString() {
        return "UpdateResponsePage [pageNumber=" + pageNumber + ", groupId=" + groupId + ", pageKey=" + pageKey + "]";
    }
}
