import requests

line_notify_token = 'TISkAnHL2gvjYg9AXxa1zHuz1z5t5hfYmLxo5Y7Uip5'
line_notify_api = 'https://notify-api.line.me/api/notify'
message = 'simulation finished'


payload = {'message': message}
headers = {'Authorization': 'Bearer ' + line_notify_token} 
line_notify = requests.post(line_notify_api, data=payload, headers=headers)


