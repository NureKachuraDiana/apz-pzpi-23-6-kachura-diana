Запити до ШІ

1.	Допоможи сформулювати висновки до звіту про архітектуру програмної системи YouTube. 
2.	Опиши основні компоненти системи YouTube для презентації та звіту.
3.	Поясни, як у YouTube відбувається взаємодія компонентів під час завантаження та перегляду відео.
4.	Допоможи придумати структуру презентації на 10 слайдів на тему «Архітектура програмної системи YouTube».

  Приклад програмного коду

1  import requests
2
3  class YouTubeApiService:
4      def __init__(self, api_key: str):
5          self.api_key = api_key
6          self.base_url = "https://www.googleapis.com/youtube/v3"
7
8      def get_video_info(self, video_id: str):
9          url = f"{self.base_url}/videos"
10         params = {
11             "part": "snippet,statistics",
12             "id": video_id,
13             "key": self.api_key
14         }
15
16         response = requests.get(url, params=params)
17         return response.json()
18
19
20  service = YouTubeApiService("YOUR_API_KEY")
21  result = service.get_video_info("dQw4w9WgXcQ")
22  print(result)
