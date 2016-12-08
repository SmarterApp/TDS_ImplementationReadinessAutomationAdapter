package org.cresst.sb.irp.automation.adapter.student.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApprovalInfo {

    @JsonProperty("status")
    private OpportunityApprovalStatus _status;

    @JsonProperty("comment")
    private String _comment;

    @JsonProperty("segmentsAccommodations")
    private List<Accommodations> _segmentsAccommodations;


    public ApprovalInfo() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ApprovalInfo{");
        sb.append("_status=").append(_status);
        sb.append(", _comment='").append(_comment).append('\'');
        sb.append(", _segmentsAccommodations=").append(_segmentsAccommodations);
        sb.append('}');
        return sb.toString();
    }

    /**
     * @return the _status
     */
    public int getNumericStatus() {
        return getStatus().ordinal();
    }

    @JsonIgnore
    public OpportunityApprovalStatus getStatus() {
        return _status;
    }

    /**
     * @param _status the _status to set
     */
    public void setStatus(OpportunityApprovalStatus _status) {
        this._status = _status;
    }

    /**
     * @return the _comment
     */
    public String getComment() {
        return _comment;
    }

    /**
     * @param _comment the _comment to set
     */
    public void setComment(String _comment) {
        this._comment = _comment;
    }

    /**
     * @return the _segmentsAccommodations
     */
    public List<Accommodations> getSegmentsAccommodations() {
        return _segmentsAccommodations;
    }

    /**
     * @param _segmentsAccommodations the _segmentsAccommodations to set
     */
    public void setSegmentsAccommodations(List<Accommodations> _segmentsAccommodations) {
        this._segmentsAccommodations = _segmentsAccommodations;
    }

    public ApprovalInfo(OpportunityStatus oppStatus) {
        // map the opportunity status to what we understand as the approval status
        switch (oppStatus.getStatus()) {
            case Approved:
                _status = OpportunityApprovalStatus.Approved;
                break;
            case Denied:
                _status = OpportunityApprovalStatus.Denied;
                break;
            case Paused:
                _status = OpportunityApprovalStatus.Logout;
                break;
            default:
                _status = OpportunityApprovalStatus.Waiting;
                break;
        }

        _comment = oppStatus.getComment();
    }

    public enum OpportunityApprovalStatus {
        Waiting(0), Approved(1), Denied(2), Logout(3);

        private int _code;

        OpportunityApprovalStatus(int code) {
            _code = code;
        }

        public int getCode() {
            return _code;
        }
    }
    //TODO
//  public enum OpportunityApprovalStatus
//  {
//      Waiting = 0,
//      Approved = 1,
//      Denied = 2,
//      Logout = 3
//  }
}