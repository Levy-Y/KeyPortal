![wakatime](https://wakatime.com/badge/user/97539eb3-1c04-4900-b9fa-d76edc9c94da/project/aeb85c01-aa12-4794-ba2f-8525590c2bf8.svg?style=for-the-badge)
![Quarkus](https://img.shields.io/badge/quarkus-%234794EB.svg?style=for-the-badge&logo=quarkus&logoColor=white) ![RabbitMQ](https://img.shields.io/badge/Rabbitmq-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white) ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

# Keyportal (Server)

**Keyportal** is a self-service SSH key management platform designed to automate and simplify key distribution in small to medium-sized organizations.

## Overview

Keyportal automates the SSH key provisioning process, reducing the manual workload for system administrators. It enables users to request access and manage their keys through an intuitive interface, while admins retain full control and visibility.

## Demo

![Ui Demo video](/media/ui_demo.gif)

## Features

- Server-rendered management panel for users and administrators
- Agent-based key installation on remote servers
- Live audit logging of actions and access
- User-friendly request interface and key management UI

## Tech Stack

- **Backend:** Java (Quarkus)
- **Frontend:** HTML, CSS, JavaScript
- **Database:** PostgreSQL
- **Message Broker:** RabbitMQ
- **Runners:**
    - Agent: systemd service
    - Server: Docker container

## Getting Started

### Prerequisites

Ensure the following tools are installed:

- `git`
- `make`
- `docker`

For Debian-based systems:
```sh
sudo apt-get install git make
```
For Docker, follow the official [guide](https://docs.docker.com/engine/install/)

### Installation

1.  Clone the repository:
    ```sh
    git clone https://github.com/Levy-Y/KeyPortal
    cd KeyPortal
    ```
2.  Configure environment:  
    Edit the `.env` file with your preferred credentials.
    ```sh
    nano .env
    ```
3. Run the setup script:
   ```sh
   make setup
   ```
   _(Note: This will prompt for your sudo password)_

### Usage

After setup completes successfully, the server will be running locally. You can now access the management panel in your browser at `http://localhost:80/management/admin`, or the key request page at `http://localhost:80/`

## Configuration

Environment variables in `.env` control:

- Database credentials and database name  
- RabbitMQ credentials

Add the server names in the `docker-compose.yml` file's `keyportal` service `environment` section that match the agent names you configured.  
If you have not configured any agents yet, configure at least one according to the [guide](https://github.com/Levy-Y/KeyPortal-Agent)

## Troubleshooting

Common issues:
- Port conflicts: Ensure port 80 is available or modify the docker-compose.yml
- Database errors: Verify PostgreSQL credentials in .env

## API Documentation
The documentation can be found [here](https://Levy-Y.github.io/KeyPortal/)

A system architecture diagram is available [here](/media/KeyPortal_diagram.png) to illustrate the key components and their interactions.

## Roadmap / Future Improvements

-   Authentication for both admin and user sides.

-   Email notifications


## License

This project is licensed under the AGPL-3.0 License.

## Author

**Levente Hagymási**
<br>
GitHub: [@Levy-Y](https://github.com/Levy-Y) <br>
LinkedIn: [in/leventehagymasi](https://www.linkedin.com/in/leventehagymasi)
