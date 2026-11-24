package ru.mondeo.drive;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
/** Native, static Mondeo artwork: no WebView or continuous animation. */
public final class MondeoSceneView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Path clip=new Path();
    private Bitmap image;
    public MondeoSceneView(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(image==null)try{
            BitmapFactory.Options o=new BitmapFactory.Options();
            o.inSampleSize=2;o.inPreferredConfig=Bitmap.Config.RGB_565;
            image=BitmapFactory.decodeResource(getResources(),R.drawable.mondeo_startup,o);
        }catch(Exception ignored){}
    }
    @Override protected void onDetachedFromWindow(){image=null;super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();if(w<1||h<1)return;
        float rad=Math.min(h*.15f,18f*getResources().getDisplayMetrics().density);
        RectF box=new RectF(1,1,w-1,h-1);
        clip.reset();clip.addRoundRect(box,rad,rad,Path.Direction.CW);
        c.save();c.clipPath(clip);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,0,w,h,new int[]{0xFF03101C,0xFF0A2540,0xFF020710},null,Shader.TileMode.CLAMP));
        c.drawRect(box,p);p.setShader(null);
        if(image!=null){
            int iw=image.getWidth(),ih=image.getHeight();
            Rect src=new Rect(Math.round(iw*.10f),Math.round(ih*.37f),Math.round(iw*.85f),Math.round(ih*.94f));
            RectF dest=new RectF(w*.19f,0,w*.84f,h);
            p.setAlpha(235);c.drawBitmap(image,src,dest,p);p.setAlpha(255);
        }
        p.setShader(new LinearGradient(0,0,w,0,new int[]{0xB703111D,0x0003111D,0xD9020B16},null,Shader.TileMode.CLAMP));
        c.drawRect(box,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,h*.012f));p.setColor(0x663DABFB);
        c.drawLine(rad+10,h-2,w-rad-10,h-2,p);
        c.restore();
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,w*.0016f));p.setColor(0xBB3A79AA);
        c.drawRoundRect(box,rad,rad,p);
        p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        p.setTextAlign(Paint.Align.LEFT);p.setColor(0xFFD5EDFF);
        p.setTextSize(Math.max(11f,h*.175f));c.drawText("MONDEO III",w*.045f,h*.34f,p);
        p.setColor(0xFF67BFFF);p.setTextSize(Math.max(8f,h*.11f));
        c.drawText("PREMIUM DRIVE",w*.045f,h*.51f,p);
        p.setTextAlign(Paint.Align.RIGHT);p.setColor(0xFFE9F5FF);
        p.setTextSize(Math.max(10f,h*.16f));c.drawText("М396ТМ 22",w*.96f,h*.34f,p);
        p.setColor(0xFF73B4DC);p.setTextSize(Math.max(8f,h*.095f));
        c.drawText("FORD  ·  2006",w*.96f,h*.51f,p);
    }
}