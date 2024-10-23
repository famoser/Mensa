import requests
import time
import re

def parse_menu_url(menu_id):
    url = f"https://zfv.ch/de/menus/rssMenuPlan?menuId={menu_id}&type=uzh2&dayOfWeek=2"
    try:
        response = requests.get(url)
        response.raise_for_status()  # Raise an exception for bad status codes
    except requests.RequestException as e:
        print(f"Error fetching for {menu_id}", flush=True)
        return

    pattern = r'<title type="text">(.*?)</title>'
    matches = re.findall(pattern, response.content.decode('utf-8'))

    for match in matches:
        print(f'{match}, MENUID: {menu_id}', flush=True)

if __name__ == "__main__":
    for menu_id in range(1, 1000):
        parse_menu_url(menu_id)
        time.sleep(1)