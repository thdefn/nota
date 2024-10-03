import yaml
import os

env = os.getenv("ENV", "local")
CONFIG_FILE_PATH = os.path.join(os.path.dirname(__file__), "config.yml")

with open(CONFIG_FILE_PATH, "r") as file:
    CONFIG = yaml.safe_load(file).get(env)

KAFKA_BOOTSTRAP_SERVERS = CONFIG['kafka']['bootstrap_servers']
KAFKA_GROUP_ID = CONFIG['kafka']['group_id']
KAFKA_TOPICS = CONFIG['kafka']['topics']