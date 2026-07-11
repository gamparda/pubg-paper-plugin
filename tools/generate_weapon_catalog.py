
import yaml, re, urllib.parse, pathlib, random
p=pathlib.Path('src/main/resources/config.yml')
data=yaml.safe_load(p.read_text(encoding='utf-8'))
G={
'AR 7.62': ['AKM','Groza','Beryl M762','Mk47 Mutant','ACE32','AK ALFA','Honey Badger'],
'AR 5.56': ['M416','M16A4','SCAR-L','AUG','QBZ','G36C','K2','FAMAS','ASM Abakan','L85A3','MCX'],
'DMR 5.56': ['Mini-14','Mk12','SL8','QBU'],
'DMR 7.62': ['SKS','Mk14','SLR','Dragunov','M110A1','M1 Garand'],
'DMR 9mm': ['VSS'],
'SR 7.62': ['Kar98k','M24','DSR-1','DSR Sniper','Mosin Nagant'],
'SR .300': ['AWM'], 'SR .45': ['Win94'], 'SR .50': ['Lynx AMR'],
'SMG 9mm': ['Micro UZI','UMP45','Tommy Gun','Vector','MP5K','MP9','JS9','PP-19 Bizon'],
'SMG 5.7': ['P90'],
'SG 12g': ['S1897','S686','S12K','DBS','M1014','NS2000','MP155 Ultima'],
'SG slug': ['O12'],
'LMG 5.56':['M249'], 'LMG 7.62':['MG3','DP-28','M134 Minigun','MG5'],
'CROSSBOW bolt':['Crossbow','Tactical Crossbow'],
'LAUNCHER rocket':['Panzerfaust','RPG-7','M3E1-A','AT4-A','M202 Bazooka'],
'LAUNCHER mortar':['Mortar'], 'LAUNCHER grenade':['M32 MGL','M79'],
'PISTOL 9mm':['P92','P18C','Skorpion','Dual MP7'],
'PISTOL .45':['Deagle','P1911','R45'], 'PISTOL 7.62':['R1895'], 'PISTOL 12g':['Sawed-off'],
'FLARE flare':['Flare Gun','Green Flare Gun'],
'MELEE none':['Sickle','Machete','Crowbar','Pickaxe','Frying Pan'],
'THROWABLE none':['Frag Grenade','Smoke Grenade','Stun Grenade','Molotov Cocktail','Sticky Bomb','Bluezone Grenade','C4']}
ko={'Groza':'그로자','Beryl M762':'베릴 M762','Mk47 Mutant':'Mk47 뮤턴트','Mini-14':'미니-14','Dragunov':'드라구노프','Mosin Nagant':'모신 나강','Lynx AMR':'링스 AMR','Micro UZI':'마이크로 UZI','Tommy Gun':'토미 건','Crossbow':'석궁','Tactical Crossbow':'전술 석궁','Panzerfaust':'판처파우스트','Mortar':'박격포','Sawed-off':'소드오프','Flare Gun':'플레어건','Green Flare Gun':'그린 플레어건','Sickle':'낫','Machete':'마체테','Crowbar':'쇠지렛대','Pickaxe':'곡괭이','Frying Pan':'프라이팬','Frag Grenade':'세열수류탄','Smoke Grenade':'연막탄','Stun Grenade':'섬광탄','Molotov Cocktail':'화염병','Sticky Bomb':'점착폭탄','Bluezone Grenade':'블루존 수류탄'}
def slug(s):
 x=s.lower().replace('.','').replace(' ','-').replace('/','-')
 return re.sub('[^a-z0-9-]','',x).strip('-')
profiles={
'AR':(43,55,145,.58,700,30,['single','auto']), 'DMR':(52,90,210,.68,360,20,['single']),
'SR':(79,120,260,.72,55,5,['single']), 'SMG':(35,25,90,.45,850,30,['single','auto']),
'SG':(18,12,55,.35,90,5,['single']), 'LMG':(44,60,170,.58,700,75,['auto']),
'CROSSBOW':(105,80,180,.65,35,1,['single']), 'LAUNCHER':(120,10,100,.75,20,1,['single']),
'PISTOL':(38,30,90,.5,400,15,['single']), 'FLARE':(0,10,100,1,20,1,['single']),
'MELEE':(45,3,4,1,80,1,['single']), 'THROWABLE':(100,4,35,.5,20,1,['single'])}
ammo_map={'5.56':'ammo-556','7.62':'ammo-762','9mm':'ammo-9','5.7':'ammo-57','.300':'ammo-300','.45':'ammo-45','.50':'ammo-50','12g':'ammo-12g','slug':'ammo-12g-slug','bolt':'ammo-bolt','rocket':'ammo-rocket','mortar':'ammo-mortar','grenade':'ammo-40mm','flare':'ammo-flare','none':'none'}
materials=['IRON_HORSE_ARMOR','GOLDEN_HORSE_ARMOR','DIAMOND_HORSE_ARMOR','LEATHER_HORSE_ARMOR','BLAZE_ROD','BRUSH','SPYGLASS','MACE']
weapons={}; idx=0
for group,names in G.items():
 typ,cal=group.split(' ',1); base,fall,rng,minmul,rpm,mag,modes=profiles[typ]
 for j,name in enumerate(names):
  wid=slug(name); variance=((sum(map(ord,name))%7)-3)
  dmg=max(1,base+variance)
  if name in ['AWM']: dmg=105
  if name in ['Lynx AMR']: dmg=118
  if name in ['Kar98k','M24','Mosin Nagant']: dmg={'Kar98k':79,'M24':75,'Mosin Nagant':79}[name]
  if name=='Deagle': dmg=62
  if name=='Win94': dmg=66
  if name=='Frying Pan': dmg=60
  if name=='M134 Minigun': rpm=1200;mag=100;dmg=32
  if name=='MG3': rpm=990
  if name in ['FAMAS','Groza','P90','AWM','Mk14','DBS','MG3','Lynx AMR']: supply=True
  else:supply=False
  actual_modes=list(modes)
  if name in ['M16A4','Mk47 Mutant','ASM Abakan']: actual_modes=['single','burst']
  if name in ['P18C','Skorpion']:actual_modes=['single','auto']
  entry={'name':'&f'+ko.get(name,name),'aliases':list(dict.fromkeys([name.lower(),ko.get(name,name)])),'category':typ.lower(),'caliber':cal,'material':materials[idx%len(materials)],'ammo-id':ammo_map[cal],'base-damage':float(dmg),'falloff-start':float(fall),'max-range':float(rng),'minimum-damage-multiplier':float(minmul),'head-multiplier':2.0 if typ not in ['SG','LAUNCHER','THROWABLE'] else 1.0,'body-multiplier':1.0,'limb-multiplier':.75,'magazine':mag,'rpm':rpm,'cooldown-ticks':max(1,round(1200/rpm)),'fire-modes':actual_modes,'burst-size':3,'weight':12 if typ in ['AR','DMR','SR','LMG'] else 7,'supply-only':supply,'source-url':'https://namu.wiki/w/'+urllib.parse.quote(ko.get(name,name)+'(PUBG: BATTLEGROUNDS)')}
  if typ=='SG':entry.update({'pellets':9,'spread-degrees':7.0})
  if typ in ['LAUNCHER','THROWABLE']:entry.update({'explosive-radius':6.0,'fuse-ticks':60})
  if name=='Smoke Grenade':entry.update({'effect':'smoke','base-damage':0.0,'explosive-radius':8.0})
  if name=='Stun Grenade':entry.update({'effect':'flash','base-damage':0.0})
  if name=='Molotov Cocktail':entry.update({'effect':'fire','base-damage':25.0})
  if name=='Bluezone Grenade':entry.update({'effect':'bluezone','base-damage':15.0})
  if typ=='MELEE':entry.update({'melee':True,'ammo-id':'none','magazine':1})
  if typ=='FLARE':entry.update({'effect':'flare','base-damage':0.0})
  weapons[wid]=entry;idx+=1
# ammo catalog
ammo=data.get('ammo',{})
for aid,name,mat,w in [('ammo-9','9mm 탄약','GOLD_NUGGET',.375),('ammo-57','5.7mm 탄약','COPPER_INGOT',.4),('ammo-300','.300 매그넘','AMETHYST_SHARD',1),('ammo-50','.50 BMG','NETHERITE_SCRAP',2),('ammo-12g','12게이지','FIREWORK_STAR',1.25),('ammo-12g-slug','12게이지 슬러그','FIRE_CHARGE',1.25),('ammo-bolt','석궁용 화살','ARROW',2),('ammo-rocket','로켓 탄두','TNT',20),('ammo-mortar','60mm 박격포탄','HEAVY_CORE',12),('ammo-40mm','40mm 유탄','SNOWBALL',8),('ammo-flare','신호탄','FIREWORK_ROCKET',5)]:ammo[aid]={'name':'&e'+name,'material':mat,'weight':w}
data['weapons']=weapons;data['ammo']=ammo
p.write_text(yaml.safe_dump(data,allow_unicode=True,sort_keys=False,width=160),encoding='utf-8')
print('weapons',len(weapons),'ammo',len(ammo))
