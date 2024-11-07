import requests

for i in range(9, 101):
    data = {
        "username": f'user_id__{i}',
        "email": f'tth{i}@gmail.com',
        "password": 12345678
    }
    res = requests.post("http://localhost:8080/public/register", json=data)
    print(res)