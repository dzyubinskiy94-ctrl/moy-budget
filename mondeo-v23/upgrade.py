from pathlib import Path
root=Path("app/src/main/java/ru/mondeo/drive")
f=root/"MainActivity.java"
s=f.read_text()
assert 'actions.addView(smallRow,new LinearLayout.LayoutParams(-1,0,.97f));' in s
assert 'for(int i=0;i<quick.length;i++) addTile(smallRow,quick[i],false,.20f,i<quick.length-1);' in s
s=s.replace('III   /   PREMIUM DRIVE 2.1','III  /  PREMIUM DRIVE 2.3 · LITE')
s=s.replace('new LinearLayout.LayoutParams(-1,0,1.03f)','new LinearLayout.LayoutParams(-1,0,.92f)')
s=s.replace('actions.addView(smallRow,new LinearLayout.LayoutParams(-1,0,.97f));','actions.addView(smallRow,new LinearLayout.LayoutParams(-1,0,.78f));')
s=s.replace('for(int i=0;i<quick.length;i++) addTile(smallRow,quick[i],false,.20f,i<quick.length-1);',
'''for(int i=0;i<quick.length;i++) addTile(smallRow,quick[i],false,.20f,i<quick.length-1);
        MondeoSceneView scene=new MondeoSceneView(this);
        LinearLayout.LayoutParams sceneLp=new LinearLayout.LayoutParams(-1,0,.72f);
        sceneLp.topMargin=Ui.dp(this,tiny?6:10);
        actions.addView(scene,sceneLp);''')
s=s.replace('MONDEO DRIVE 2.2','MONDEO DRIVE 2.3')
f.write_text(s)
f=root/"PremiumTileDrawable.java"
s=f.read_text().replace('int top=active?0xFF173551:0xFF132A42, bottom=active?0xFF0B1930:0xFF071527;','int top=active?0xFF193D62:0xFF142A45, bottom=active?0xFF071323:0xFF040D1C;')
s=s.replace('new int[]{top,0xFF10253D,bottom}','new int[]{top,0xFF102C49,bottom}').replace('active?0xFF5FA9DD:0xFF355876','active?0xFF79C9FF:0xFF3F6A8C').replace('active?0x7FB9E9FF:0x607DAED5','active?0x9FD2F1FF:0x527DAED5')
f.write_text(s)
f=Path('app/build.gradle');s=f.read_text().replace('versionCode 22','versionCode 23').replace("versionName '2.2'","versionName '2.3'");f.write_text(s)
print('Applied Premium Lite visual update')
