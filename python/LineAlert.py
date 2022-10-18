import requests
import os
from dotenv import load_dotenv

load_dotenv()
LINE_SIMULATION_NOTIFIER_TOKEN = os.getenv('LINE_SIMULATION_NOTIFIER_TOKEN')
line_notify_api = 'https://notify-api.line.me/api/notify'

path = os.getcwd()
message = path


payload = {'message': message}
headers = {'Authorization': 'Bearer ' + LINE_SIMULATION_NOTIFIER_TOKEN} 
line_notify = requests.post(line_notify_api, data=payload, headers=headers)
