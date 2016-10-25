package org.cresst.sb.irp.automation.adapter.student.data;

public class UpdateResponsePage {
    private int pageNumber;
    private String groupId;
    private String pageKey;
    public UpdateResponsePage(String groupId, String pageKey, int pageNumber) {
        this.pageNumber = pageNumber;
        this.groupId = groupId;
        this.pageKey = pageKey;
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
    @Override
    public String toString() {
        return "UpdateResponsePage [pageNumber=" + pageNumber + ", groupId=" + groupId + ", pageKey=" + pageKey + "]";
    }
}
