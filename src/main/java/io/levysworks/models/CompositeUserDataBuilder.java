package io.levysworks.models;

import java.sql.Timestamp;

/**
 * Builder for {@link CompositeUserData}, allowing incremental construction
 * of user-related composite data with optional fields.
 */
public class CompositeUserDataBuilder {
    private String initials = null;
    private String username = null;
    private String uuid = null;
    private String first_name = null;
    private String last_name = null;
    private String email = null;
    private String department = null;
    private String notes = null;
    private Integer key_count = null;
    private String servers = null;
    private Integer request_id = null;
    private String key_type = null;
    private String key_uid = null;
    private String server = null;
    private String public_key = null;
    private String fingerprint = null;
    private String accepted_by = null;
    private Timestamp issued_date = null;
    private Timestamp valid_until = null;
    private String log_title = null;
    private String log_message = null;
    private Timestamp log_timestamp = null;

    public CompositeUserDataBuilder initials(String initials) {
        this.initials = initials;
        return this;
    }

    public CompositeUserDataBuilder username(String username) {
        this.username = username;
        return this;
    }

    public CompositeUserDataBuilder uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public CompositeUserDataBuilder first_name(String first_name) {
        this.first_name = first_name;
        return this;
    }

    public CompositeUserDataBuilder last_name(String last_name) {
        this.last_name = last_name;
        return this;
    }

    public CompositeUserDataBuilder email(String email) {
        this.email = email;
        return this;
    }

    public CompositeUserDataBuilder department(String department) {
        this.department = department;
        return this;
    }

    public CompositeUserDataBuilder notes(String notes) {
        this.notes = notes;
        return this;
    }

    public CompositeUserDataBuilder key_count(Integer key_count) {
        this.key_count = key_count;
        return this;
    }

    public CompositeUserDataBuilder servers(String servers) {
        this.servers = servers;
        return this;
    }

    public CompositeUserDataBuilder request_id(Integer request_id) {
        this.request_id = request_id;
        return this;
    }

    public CompositeUserDataBuilder key_type(String key_type) {
        this.key_type = key_type;
        return this;
    }

    public CompositeUserDataBuilder key_uid(String uid) {
        this.key_uid = uid;
        return this;
    }

    public CompositeUserDataBuilder server(String server) {
        this.server = server;
        return this;
    }

    public CompositeUserDataBuilder public_key(String public_key) {
        this.public_key = public_key;
        return this;
    }

    public CompositeUserDataBuilder fingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
        return this;
    }

    public CompositeUserDataBuilder accepted_by(String accepted_by) {
        this.accepted_by = accepted_by;
        return this;
    }

    public CompositeUserDataBuilder issued_date(Timestamp issued_date) {
        this.issued_date = issued_date;
        return this;
    }

    public CompositeUserDataBuilder valid_until(Timestamp valid_until) {
        this.valid_until = valid_until;
        return this;
    }

    public CompositeUserDataBuilder log_title(String log_title) {
        this.log_title = log_title;
        return this;
    }

    public CompositeUserDataBuilder log_message(String log_message) {
        this.log_message = log_message;
        return this;
    }

    public CompositeUserDataBuilder log_timestamp(Timestamp log_timestamp) {
        this.log_timestamp = log_timestamp;
        return this;
    }

    /**
     * Builds a {@link CompositeUserData} instance using the set values.
     *
     * @return a new {@code CompositeUserData} instance
     */
    public CompositeUserData build() {
        return new CompositeUserData(initials, username, uuid, first_name, last_name, email, department, notes, key_count, servers, request_id, key_type, key_uid, server, public_key, fingerprint, accepted_by, issued_date, valid_until, log_title, log_message, log_timestamp);
    }
}
