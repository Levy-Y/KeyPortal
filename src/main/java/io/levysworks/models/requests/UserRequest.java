package io.levysworks.models.requests;

/**
 * Represents a user request containing personal and organizational details.
 *
 * @param first_name user's first name
 * @param last_name user's last name
 * @param email email address
 * @param department department or organizational unit
 * @param notes Additional notes or comments related to the user
 */
public record UserRequest(String first_name, String last_name, String email, String department, String notes) {}
