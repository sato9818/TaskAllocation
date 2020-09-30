from pydrive.auth import GoogleAuth
from pydrive.drive import GoogleDrive

gauth = GoogleAuth()
gauth.LocalWebserverAuth()

drive = GoogleDrive(gauth)
f = drive.CreateFile()
f.SetContentFile('python/csvToGraph_area.py')

f.Upload()