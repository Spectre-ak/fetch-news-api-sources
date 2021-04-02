import pandas as pd
from newsapi import NewsApiClient

api = NewsApiClient(api_key='xxxxxx')
src=api.get_sources()

srcId=[]
name=[]
description=[]
url=[]
category=[]
language=[]
country=[]

for obj in src['sources']:
    print(obj['url'])
    srcId.append(obj['id'])
    name.append(obj['name'])
    description.append(obj['description'])
    url.append(obj['url'])
    category.append(obj['category'])
    language.append(obj['language'])
    country.append(obj['country'])
    
df=pd.DataFrame()
df['srcId']=srcId
df['name']=name
df['description']=description
df['url']=url
df['category']=category
df['language']=language
df['country']=country

df.to_csv("sources.csv")
