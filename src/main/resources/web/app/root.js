import axios from 'axios';

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

document.getElementById('sshKeyForm').addEventListener('submit', async e => {
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