from pathlib import Path
p=Path("app/src/main/java/ru/mondeo/drive")
m=p/"MainActivity.java"
s=m.read_text()
s=s.replace("import android.graphics.Color;","import android.graphics.Color;\nimport android.content.res.ColorStateList;\nimport android.graphics.drawable.RippleDrawable;")
s=s.replace('TextView mark=Ui.text(this,"M",tiny?21:27,Ui.WHITE,true);\n        mark.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD_ITALIC));\n        mark.setGravity(Gravity.CENTER);\n        mark.setBackground(Ui.gradient(this,0xFF205382,0xFF14314B,14,0xFF4A96C9));\n        top.addView(mark,new LinearLayout.LayoutParams(Ui.dp(this,tiny?37:48),Ui.dp(this,tiny?37:48)));','FordBadgeView mark=new FordBadgeView(this);\n        top.addView(mark,new LinearLayout.LayoutParams(Ui.dp(this,tiny?77:108),Ui.dp(this,tiny?37:48)));')
s=s.replace('"III   /   DRIVE 2.0"','"III   /   PREMIUM DRIVE 2.1"')
s=s.replace('Ui.touch(tile,this,highlight?0xFF17334C:Ui.SURFACE,17,highlight?0xFF315E80:0xFF2B4052);','tile.setBackground(new RippleDrawable(ColorStateList.valueOf(0x3359B7FF),new PremiumTileDrawable(highlight,accent),null));\n        Ui.touch(tile);')
s=s.replace('bar.setBackground(Ui.gradient(this,0xFF142438,0xFF0E1B2B,17,0xFF30485E));','bar.setBackground(new PremiumTileDrawable(false,accent));')
m.write_text(s)
g=Path("app/build.gradle")
g.write_text(g.read_text().replace("versionCode 20","versionCode 21").replace("versionName '2.0'","versionName '2.1'"))
(p/"FordBadgeView.java").write_text("""package ru.mondeo.drive;
import android.content.Context;
import android.graphics.*;
import android.view.View;
public final class FordBadgeView extends View{
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  public FordBadgeView(Context c){super(c);setContentDescription("Ford");}
  @Override protected void onDraw(Canvas c){
    float w=getWidth(),h=getHeight();if(w<1||h<1)return;
    p.setStyle(Paint.Style.FILL);
    p.setShader(new LinearGradient(0,0,w,h,0xFF185AA0,0xFF031A3B,Shader.TileMode.CLAMP));
    c.drawOval(new RectF(2,2,w-2,h-2),p);p.setShader(null);
    p.setStyle(Paint.Style.STROKE);p.setColor(0xFFA8D7FF);p.setStrokeWidth(Math.max(1.5f,h*.045f));
    c.drawOval(new RectF(3,3,w-3,h-3),p);
    p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);
    p.setTypeface(Typeface.create("serif",Typeface.BOLD_ITALIC));p.setTextSize(h*.61f);
    p.setColor(Color.WHITE);c.drawText("Ford",w*.49f,h*.72f,p);
  }
}
""")
(p/"PremiumTileDrawable.java").write_text("""package ru.mondeo.drive;
import android.graphics.*;
import android.graphics.drawable.Drawable;
public final class PremiumTileDrawable extends Drawable{
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final boolean active;private final int accent;
  public PremiumTileDrawable(boolean active,int accent){this.active=active;this.accent=accent;}
  @Override public void draw(Canvas c){
    Rect b=getBounds();float w=b.width(),h=b.height();if(w<=0||h<=0)return;
    float radius=Math.min(24,Math.min(w,h)*.14f);
    RectF r=new RectF(b.left+1,b.top+1,b.right-1,b.bottom-1);
    p.setStyle(Paint.Style.FILL);
    p.setShader(new LinearGradient(b.left,b.top,b.right,b.bottom,
      new int[]{active?0xFF193F60:0xFF142B42,0xFF0E2238,0xFF071324},null,Shader.TileMode.CLAMP));
    c.drawRoundRect(r,radius,radius,p);p.setShader(null);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,w*.004f));
    p.setColor(active?0xFF529BD3:0xFF355774);c.drawRoundRect(r,radius,radius,p);
    p.setStrokeWidth(Math.max(1,h*.006f));p.setColor(0x686EACDA);
    c.drawLine(b.left+radius+6,b.top+2,b.right-radius-6,b.top+2,p);
    p.setColor((accent&0x00FFFFFF)|0x85000000);p.setStrokeWidth(Math.max(1,h*.008f));
    c.drawLine(b.left+radius+7,b.bottom-2,b.right-radius-7,b.bottom-2,p);
    p.setStyle(Paint.Style.FILL);
  }
  @Override public void setAlpha(int value){p.setAlpha(value);invalidateSelf();}
  @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}
  @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
""")
bg=p/"DashboardBackground.java";t=bg.read_text()
t=t.replace("import android.graphics.Shader;","import android.graphics.Shader;\nimport android.graphics.RadialGradient;\nimport android.graphics.Path;")
start=t.index("    @Override protected void onDraw(Canvas c){")
t=t[:start]+"""    @Override protected void onDraw(Canvas c){
        float w=getWidth(),h=getHeight();
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,0,w,h,new int[]{0xFF050C18,0xFF08192B,0xFF030813},null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setShader(new RadialGradient(w*.69f,h*.12f,w*.7f,new int[]{0x32499DF1,0x0B12395C,0x00000000},null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,w*.0011f));p.setColor(0x303F9DDE);
        Path path=new Path();path.moveTo(w*.22f,h*.12f);path.lineTo(w*.56f,h*.12f);path.lineTo(w*.61f,h*.04f);c.drawPath(path,p);
        p.setStyle(Paint.Style.FILL);
    }
}
"""
bg.write_text(t)
sg=p/"SpeedGaugeView.java";t=sg.read_text()
t=t.replace("p.setStrokeWidth(size*.027f);p.setColor(0xFF30485E);c.drawArc(rect,135,270,false,p);","p.setStrokeWidth(size*.059f);p.setColor(0x194AAAFB);c.drawArc(rect,135,270,false,p);\n        p.setStrokeWidth(size*.027f);p.setColor(0xFF30485E);c.drawArc(rect,135,270,false,p);")
t=t.replace("p.setColor(accent);c.drawArc(rect,135,270f*Math.min(kmh,220)/220f,false,p);","p.setColor(accent);c.drawArc(rect,135,270f*Math.min(kmh,240)/240f,false,p);")
t=t.replace("for(int i=0;i<=18;i++){","for(int i=0;i<=48;i++){").replace("135+i*15d","135+i*270d/48").replace("i%3==0","i%8==0").replace("i*220f/18f","i*5f")
t=t.replace('p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);','''p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        p.setTextSize(size*.052f);p.setColor(0xFFD3E6F8);
        for(int i=0;i<=6;i++){
            double a=Math.toRadians(135+i*45d);
            c.drawText(Integer.toString(i*40),cx+(float)Math.cos(a)*rad*.82f,
                cy+(float)Math.sin(a)*rad*.82f+size*.019f,p);
        }''')
sg.write_text(t)
print("Premium visual overlay applied")
