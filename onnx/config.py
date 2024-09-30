import yaml
import os

CONFIG_FILE_PATH = os.path.join(os.path.dirname(__file__), "config.yml")

with open(CONFIG_FILE_PATH, "r") as file:
    CONFIG = yaml.safe_load(file)

KAFKA_BOOTSTRAP_SERVERS = CONFIG['kafka']['bootstrap_servers']
KAFKA_GROUP_ID = CONFIG['kafka']['group_id']
KAFKA_TOPICS = CONFIG['kafka']['topics']