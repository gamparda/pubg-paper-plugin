#!/usr/bin/env python3
"""Refresh weapon PC damage/RPM/velocity from each configured source URL.

Falls back to the checked-in balance profile when a page has no compatible table.
The source pages are community-maintained; values remain operator-editable.
"""
from concurrent.futures import ThreadPoolExecutor, as_completed
from html.parser import HTMLParser
from pathlib import Path
import datetime, re, requests, yaml

CONFIG=Path('src/main/resources/config.yml')
HEADERS={'User-Agent':'Mozilla/5.0 (compatible; BattlegroundsConfigResearch/1.0)'}

class TableParser(HTMLParser):
    def __init__(self): super().__init__(); self.tr=0; self.td=0; self.rows=[]; self.row=[]; self.cell=[]
    def handle_starttag(self,tag,attrs):
        if tag=='tr': self.tr+=1; self.row=[]
        elif self.tr and tag in ('td','th'): self.td+=1; self.cell=[]
    def handle_data(self,data):
        if self.td:self.cell.append(data)
    def handle_endtag(self,tag):
        if tag in ('td','th') and self.td:
            self.row.append(re.sub(r'\s+',' ',' '.join(self.cell)).strip());self.td-=1
        elif tag=='tr' and self.tr:
            if self.row:self.rows.append(self.row)
            self.tr-=1

def first_numbers(text): return [float(x) for x in re.findall(r'(?<![A-Za-z])\d+(?:\.\d+)?',text.replace(',',''))]
def fetch(pair):
    wid,url=pair
    try:
        r=requests.get(url,headers=HEADERS,timeout=20);r.raise_for_status();p=TableParser();p.feed(r.text)
        found={}
        for row in p.rows:
            if len(row)<2:continue
            label=row[0].strip()
            if label in ('몸통 기본','탄속','연사 간격','발사 간격','장탄수') and label not in found:found[label]=row[1]
        return wid,found,None
    except Exception as exc:return wid,{},str(exc)

def main():
    data=yaml.safe_load(CONFIG.read_text(encoding='utf-8')); weapons=data['weapons'];pairs=[(wid,w['source-url']) for wid,w in weapons.items()]
    checked=updated=0
    with ThreadPoolExecutor(max_workers=6) as pool:
        futures=[pool.submit(fetch,p) for p in pairs]
        for f in as_completed(futures):
            wid,rows,error=f.result(); w=weapons[wid];w['source-checked-at']=datetime.date.today().isoformat();checked+=1
            nums=first_numbers(rows.get('몸통 기본',''))
            if nums:
                divisor=max(1,int(w.get('pellets',1)))
                w['base-damage']=round(nums[0]/divisor,4)
                if len(nums)>1 and nums[0]>0:w['minimum-damage-multiplier']=round(min(1,nums[1]/nums[0]),4)
                w['balance-source']='namuwiki-pc-table';updated+=1
            else:w['balance-source']='category-fallback'
            rpm=re.search(r'(\d+(?:\.\d+)?)\s*RPM',rows.get('연사 간격','')+' '+rows.get('발사 간격',''),re.I)
            if rpm:
                w['rpm']=float(rpm.group(1));w['cooldown-ticks']=max(1,round(1200/w['rpm']))
            speed=first_numbers(rows.get('탄속',''))
            if speed:w['bullet-speed-mps']=speed[0]
    CONFIG.write_text(yaml.safe_dump(data,allow_unicode=True,sort_keys=False,width=160),encoding='utf-8')
    print(f'checked={checked} table-updated={updated} fallback={checked-updated}')
if __name__=='__main__':main()
