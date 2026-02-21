package com.soen345.ticketreservation.model;

import com.google.firebase.Timestamp;

public class User {

    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_ADMIN    = "admin";

    private String uid;
    private String email;
    private String phone;
    private String displayName;
    private String role;
    private Timestamp createdAt;

    /** Required for Firestore deserialization. */
    public User() {}

    public User(String uid, String email, String phone, String displayName, String role) {
        this.uid         = uid;
        this.email       = email;
        this.phone       = phone;
        this.displayName = displayName;
        this.role        = role;
        this.createdAt   = Timestamp.now();
    }

    public String getUid()          { return uid; }
    public void   setUid(String v)  { uid = v; }

    public String getEmail()           { return email; }
    public void   setEmail(String v)   { email = v; }

    public String getPhone()           { return phone; }
    public void   setPhone(String v)   { phone = v; }

    public String getDisplayName()          { return displayName; }
    public void   setDisplayName(String v)  { displayName = v; }

    public String getRole()           { return role; }
    public void   setRole(String v)   { role = v; }

    public Timestamp getCreatedAt()            { return createdAt; }
    public void      setCreatedAt(Timestamp v) { createdAt = v; }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }
}
