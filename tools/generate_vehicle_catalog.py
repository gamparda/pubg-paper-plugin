#!/usr/bin/env python3
import yaml
from pathlib import Path
p=Path('src/main/resources/config.yml');d=yaml.safe_load(p.read_text(encoding='utf-8'))
source='https://namu.wiki/w/PUBG:%20BATTLEGROUNDS/%EC%9D%B4%EB%8F%99%EC%88%98%EB%8B%A8'
# id: Korean name, type, max/reverse kmh, turn deg/s, seats, durability, fuel, acceleration, fuel/s, armored, model
rows={
'motorcycle':('모터사이클','LAND',152,60,95,2,1000,100,.095,.020,False,'BLACK_CONCRETE'),
'scooter':('스쿠터','LAND',90,35,75,2,1000,100,.070,.017,False,'LIGHT_BLUE_CONCRETE'),
'tukshai':('툭샤이','LAND',85,30,52,3,1200,100,.050,.020,False,'YELLOW_CONCRETE'),
'mountain-bike':('산악 자전거','LAND',62,25,100,1,500,0,.085,0,False,'OAK_PLANKS'),
'snowmobile':('스노우 모빌','LAND',110,40,72,2,1200,100,.080,.022,False,'WHITE_CONCRETE'),
'dacia':('다시아','LAND',117,45,48,4,1800,100,.060,.018,False,'BLUE_CONCRETE'),
'mirado':('미라도','LAND',152,55,50,4,2000,100,.065,.023,False,'RED_CONCRETE'),
'pony-coupe':('포니 쿠페','LAND',140,50,55,4,1800,100,.070,.020,False,'CYAN_CONCRETE'),
'minibus':('미니 버스','LAND',105,35,35,6,4000,100,.035,.030,False,'ORANGE_CONCRETE'),
'uaz':('UAZ','LAND',115,40,42,4,2400,100,.050,.026,False,'GREEN_CONCRETE'),
'buggy':('버기','LAND',100,40,70,2,1540,100,.075,.020,False,'BROWN_CONCRETE'),
'brdm':('BRDM-2','AMPHIBIOUS',105,35,30,4,4000,100,.032,.035,True,'GRAY_CONCRETE'),
'motor-glider':('모터글라이더','AIR',110,25,45,2,1000,100,.040,.028,False,'LIME_CONCRETE'),
'boat':('보트','WATER',90,30,45,6,1500,100,.055,.025,False,'DARK_OAK_PLANKS'),
'aquarail':('아쿠아레일','WATER',125,35,80,2,1000,100,.080,.022,False,'PRISMARINE'),
'rubber-boat':('고무보트','WATER',60,20,50,4,1000,100,.045,.018,False,'BLACK_WOOL')}
out={}
for id,r in rows.items():
 name,t,top,rev,turn,seats,hp,fuel,acc,usage,armored,model=r
 out[id]={'name':name,'type':t,'top-speed-kmh':top,'reverse-speed-kmh':rev,'boost-multiplier':1.15,'turn-rate-deg':turn,'seats':seats,'durability':hp,'fuel-capacity':fuel,'acceleration':acc,'fuel-per-second':usage,'armored':armored,'locked-to-team':id=='brdm','takeoff-speed-kmh':60 if t=='AIR' else 0,'model-material':model,'model-scale':[1.2,0.7,2.0] if seats<=2 else [1.6,1.0,2.8],'spawn-item':'MINECART','explosion-delay-seconds':5,'explosion-radius':5 if hp<3000 else 7,'source-url':source,'balance-source':'pubg-community-vehicle-profile','source-checked-at':'2026-07-11'}
d['vehicles']=out
d['vehicle-system']={'blocks-per-meter':1.0,'exit-safe-speed-kmh':30,'exit-lethal-speed-kmh':65,'roadkill-min-speed-kmh':25,'collision-damage-multiplier':1.0,'show-actionbar':True}
p.write_text(yaml.safe_dump(d,allow_unicode=True,sort_keys=False,width=160),encoding='utf-8')
print('vehicles',len(out))
