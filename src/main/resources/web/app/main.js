import axios from 'axios';

async function saveUserProfile() {
    let first_name = document.getElementById("first-name-input").value
    let last_name = document.getElementById("last-name-input").value
    let email = document.getElementById("email-input").value
    let department = document.getElementById("department-input").value
    let notes = document.getElementById("notes-input").value

    let uuid = document.getElementById("saveChangesButton").dataset.uuid

    let request = await fetch("http://localhost:8080/api/v1/secured/users/" + uuid, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            first_name: first_name,
            last_name: last_name,
            email: email,
            department: department,
            notes: notes
        }),
    });

    if (request.ok) {
        alert("Updated user with UUID: " + uuid);
        location.reload();
    } else if (request.status === 500) {
        alert("No user found with UUID: " + uuid);
    }

}

async function requestKey(server, user) {
    try {
        const response = await axios.get("/api/v1/register?server=" + server + "&user=" + user, {
            responseType: 'arraybuffer'
        });

        if (response.status !== 200) {
            throw new Error("SSH Key request failed, with status: " + response.status);
        }

        const privateKey = new TextDecoder().decode(response.data);
        return { privateKey };
    } catch (error) {
        if (axios.isAxiosError(error) && error.response && error.response.status === 429) {
            throw new Error("Rate limit exceeded");
        }
        throw error;
    }
}

async function revokeKey(server, uid) {
    try {
        const response = await axios.delete(`/api/v1/secured/remove/${uid}?agent=${server}`);
        if (response.status === 204) {
            alert("Revoked user key with UID: " + uid);
            location.reload();
        } else {
            alert("Error occurred while trying to revoke user key with UID: " + uid);
            throw new Error(`Unexpected status: ${response.status}`);
        }
    } catch (error) {
        if (axios.isAxiosError(error)) {
            if (error.response?.status === 500) {
                throw new Error("Internal Server Error");
            }
            throw new Error(`Failed to revoke key: ${error.response?.status}`);
        }
        throw error;
    }
}

function gotoUserProfile(username) {
    location.href = '/management/admin?query_user=' + username;
}

document.addEventListener("DOMContentLoaded", () => {
    let saveChangesButton = document.getElementById("saveChangesButton");
    if (saveChangesButton) {
        saveChangesButton.onclick = saveUserProfile;
    }

    document.addEventListener("click", (event) => {
        if (event.target.classList.contains('viewUserButton')) {
            const uuid = event.target.dataset.uuid;
            gotoUserProfile(uuid);
        } else if (event.target.classList.contains('revokeKeyButton')) {
            const uid = event.target.dataset.uid;
            const server = event.target.dataset.server;
            revokeKey(server, uid)
        }
    });
});


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

            if (!keyData || !keyData.privateKey) {
                throw new Error('Invalid response: missing private key');
            }

            document.getElementById('selectedServer').textContent = server;
            document.getElementById('selectedUserId').textContent = userId;
            document.getElementById('generatedTime').textContent = new Date().toLocaleString();

            const privateKeyBlob = new Blob([keyData.privateKey], {type: 'text/plain'});
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