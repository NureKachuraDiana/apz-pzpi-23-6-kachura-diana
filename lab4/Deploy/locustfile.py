from locust import HttpUser, task, between

class BackendUser(HttpUser):
    wait_time = between(1, 3)

    @task(3)
    def test_database_endpoint(self):
        self.client.get("/monitoring-station")

