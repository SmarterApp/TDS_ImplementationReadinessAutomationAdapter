package org.cresst.sb.irp.automation.adapter.student.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApprovalInfo
{

  private OpportunityApprovalStatus _status;
  private String _comment;
  private List<String> _segmentsAccommodations;


  /**
   * @return the _status
   */
  @JsonProperty ("status")
  public int getNumericStatus()
  {
    return getStatus ().ordinal ();
  }

  @JsonIgnore
  public OpportunityApprovalStatus getStatus () {
    return _status;
  }

  /**
   * @param _status the _status to set
   */
  public void setStatus (OpportunityApprovalStatus _status) {
    this._status = _status;
  }

  /**
   * @return the _comment
   */
  @JsonProperty ("comment")
  public String getComment () {
    return _comment;
  }

  /**
   * @param _comment the _comment to set
   */
  public void setComment (String _comment) {
    this._comment = _comment;
  }

  /**
   * @return the _segmentsAccommodations
   */
  @JsonProperty ("segmentsAccommodations")
  public List<String> getSegmentsAccommodations () {
    return _segmentsAccommodations;
  }

  /**
   * @param _segmentsAccommodations the _segmentsAccommodations to set
   */
  public void setSegmentsAccommodations (List<String> _segmentsAccommodations) {
    this._segmentsAccommodations = _segmentsAccommodations;
  }

  public enum OpportunityApprovalStatus {
    Waiting(0), Approved(1), Denied(2), Logout(3);

    private int _code;

    OpportunityApprovalStatus (int code) {
      _code = code;
    }

    public int getCode () {
      return _code;
    }
  }

}