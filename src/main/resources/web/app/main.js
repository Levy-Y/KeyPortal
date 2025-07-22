import axios from 'axios';

async function saveUserProfile() {
    let first_name = document.getElementById("first-name-input").value
    let last_name = document.getElementById("last-name-input").value
    let email = document.getElementById("email-input").value
    let department = document.getElementById("department-input").value
    let notes = document.getElementById("notes-input").value

    let uuid = document.getElementById("saveChangesButton").dataset.uuid

    await axios.patch(`http://localhost:8080/api/v1/secured/users/${uuid}`, {
        first_name: first_name,
        last_name: last_name,
        email: email,
        department: department,
        notes: notes
    }, {
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => {

        if (response.status === 204) {
            alert("Updated user with UUID: " + uuid);
            location.reload();
        } else {
            alert("Failed to update user with UUID: " + uuid);
        }
    }).catch(error => {
        if (error.response && error.response.status === 500) {
            alert("No user found with UUID: " + uuid);
        }
    });

}

async function approveKey(requestId, action) {
    await axios.patch(`http://localhost:8080/api/v1/secured/requests/${requestId}`, {
        action: action
    }, {
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => {
        if (response.status === 204) {
            alert(`${action} action on ${requestId} was successful.`);
            location.reload();
        }
    }).catch(error => {
        console.error(error);
    });
}

async function requestKey(server, user) {
    let privateKey = null

    await axios.get("/api/v1/register?server=" + server + "&user=" + user, {
        responseType: 'arraybuffer'
    }).then(response => {
        if (response.status !== 200) {
            throw new Error("SSH Key request failed, with status: " + response.status);
        }

        const decoder = new TextDecoder('utf-8');
        privateKey = decoder.decode(new Uint8Array(response.data));
    }).catch(error => {
        if (axios.isAxiosError(error) && error.response && error.response.status === 429) {
            throw new Error("Rate limit exceeded");
        } else if (error.response.status === 401) {
            throw new Error("Invalid user identifier");
        }
    });

    return { privateKey };
}

async function revokeKey(server, uid) {
    await axios.delete(`/api/v1/secured/remove/${uid}?agent=${server}`).then(response => {
        if (response.status === 204) {
            alert("Revoked user key with UID: " + uid);
            location.reload();
        } else {
            alert("Error occurred while trying to revoke user key with UID: " + uid);
            throw new Error(`Unexpected status: ${response.status}`);
        }
    }).catch(error => {
        if (axios.isAxiosError(error)) {
            if (error.response?.status === 500) {
                throw new Error("Internal Server Error");
            }
            throw new Error(`Failed to revoke key: ${error.response?.status}`);
        }
        throw error;
    });
}

async function deleteUserProfile() {
    let uuid = document.getElementById("saveChangesButton").dataset.uuid;

    await axios.delete(`/api/v1/secured/users/delete?uuid=` + uuid).then(response => {
        if (response.status === 204) {
            alert("Deleted user with UUID: " + uuid);
            location.href = "/management/admin";
        } else if (response.status === 404) {
            alert("User not found with UUID: " + uuid);
        } else {
            alert("Error deleting user with UUID: " + uuid);
        }
    }).catch(error => {
        if (axios.isAxiosError(error)) {
            if (error.response && error.response.status === 500) {
                throw new Error("Internal Server Error");
            }
        }
    })
}

function gotoUserProfile(uuid) {
    location.href = '/management/admin/user/' + uuid;
}

document.addEventListener("DOMContentLoaded", () => {
    let saveChangesButton = document.getElementById("saveChangesButton");
    if (saveChangesButton) {
        saveChangesButton.onclick = saveUserProfile;
    }

    let deleteUserButton = document.getElementById("deleteUserButton");
    if (deleteUserButton) {
        deleteUserButton.onclick = deleteUserProfile;
    }

    document.addEventListener("click", (event) => {
        if (event.target.classList.contains('viewUserButton')) {
            const uuid = event.target.dataset.uuid;
            gotoUserProfile(uuid);
        } else if (event.target.classList.contains('revokeKeyButton')) {
            const uid = event.target.dataset.uid;
            const server = event.target.dataset.server;
            revokeKey(server, uid)
        } else if (event.target.classList.contains('approveKeyButton')) {
            const requestId = event.target.dataset.request_id;
            const action = event.target.dataset.action;
            approveKey(requestId, action)
        }
    });
});

let createUserForm = document.getElementById("createUserForm");
if (createUserForm) {
    createUserForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const firstName = document.getElementById("new-first-name-input").value.trim();
        const lastName = document.getElementById("new-last-name-input").value.trim();
        const email = document.getElementById("new-email-input").value.trim();
        const department = document.getElementById("new-department-input").value.trim();
        const notes = document.getElementById("new-notes-input").value.trim();

        const userData = {
            first_name: firstName,
            last_name: lastName,
            email: email,
            department: department,
            notes: notes
        };

        try {
            const response = await axios.post("/api/v1/secured/users/create", userData);
            if (response.status === 204) {
                alert("Created user profile");
                location.href = "/management/admin";
            } else {
                alert("Unexpected status code: " + response.status);
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 500) {
                alert("Server error while creating user");
            } else {
                alert("Failed to create user");
            }
        }
    });
}


let sshKeyForm = document.getElementById('sshKeyForm');
if (sshKeyForm) {
    sshKeyForm.addEventListener('submit', async e => {
        e.preventDefault();

        const server = document.getElementById('serverSelect').value;
        const userId = document.getElementById('userId').value;

        if (!server || !userId) {
            alert('Please fill in all fields');
            return;
        }

        const btn = document.getElementById('generateBtn');
        btn.disabled = true;
        btn.textContent = 'Generating...';

        try {
            const keyData = await requestKey(server, userId);

            document.getElementById('selectedServer').textContent = server;
            document.getElementById('selectedUserId').textContent = userId;
            document.getElementById('generatedTime').textContent = new Date().toLocaleString();

            const privateKeyBlob = new Blob([keyData.privateKey],
                {
                    type: 'application/x-pem-file;charset=utf-8'
                });
            document.getElementById('downloadPrivateKey').href = URL.createObjectURL(privateKeyBlob);
            document.getElementById('downloadPrivateKey').download = `${userId}_${server}_private_key.pem`;



            document.getElementById('downloadSection').classList.remove('hidden');

        } catch (error) {
            alert('Error generating SSH key: ' + error.message);
        } finally {
            btn.disabled = false;
            btn.textContent = 'Generate SSH Key';
        }
    });
}