package com.abc.product.app.bo;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class UserProfile {
	private String fullName;
	private String userId;
	private String mobileNo;
	private String emailId;
	private String password;
	private Date dateOfBirth;
	private List<Preference> preferences;

	public UserProfile() {
	}

	public UserProfile(String emailId, String fullName) {
		this.fullName = fullName;
		this.emailId = emailId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getUserId() {
		return userId;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public String getEmailId() {
		return emailId;
	}

	public String getPassword() {
		return password;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public List<Preference> getPreferences() {
		if (preferences == null) {
			preferences = new ArrayList<Preference>();
		}
		return preferences;
	}
}
