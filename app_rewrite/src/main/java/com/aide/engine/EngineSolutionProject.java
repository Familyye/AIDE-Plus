//
// Decompiled by Jadx - 614ms
//
package com.aide.engine;

import abcd.ey;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import com.aide.common.AppLog;
import com.aide.engine.EngineSolution;
import io.github.zeroaicy.util.ContextUtil;
import io.github.zeroaicy.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class EngineSolutionProject implements Parcelable {
    public static final Parcelable.Creator<EngineSolutionProject> CREATOR = new EngineSolutionProject$a();

    public final String projectName;

    final String AL;
    final List<String> Jl;
    final String Q6;
    final boolean Z1;
    final boolean cT;
    final List<EngineSolution.File> fY;
    final String hK;
    final List<String> iW;
    final String jw;
    final boolean k2;
    final List<String> kf;
    final String mb;
    final boolean n5;
    final boolean q7;
    final List<String> qp;
    final String w9;
    final String zh;


	private boolean compress = false;
    public EngineSolutionProject(String projectName, String str2, String str3, List<EngineSolution.File> list, List<String> list2, boolean z, String str4, String str5, String str6, String str7, boolean z2, boolean z3, boolean z4, boolean z5, String str8, List<String> list3, List<String> list4, List<String> list5){
		this.projectName = projectName;

        this.mb = str2;
        this.jw = str3;
        this.fY = list;
        this.qp = list2;
        this.k2 = z;
        this.zh = str4;
        this.AL = str5;
        this.w9 = str6;
        this.hK = str7;
        this.cT = z2;
        this.q7 = z3;
        this.Z1 = z4;
        this.n5 = z5;
        this.Q6 = str8;
        this.kf = list3;
        this.Jl = list4;
        this.iW = list5;
    }



    @Override
    public int describeContents(){
		return 0;
    }
	
	private static SharedPreferences sharedPreferences;
	public static SharedPreferences getSharedPreferences(){
		if ( sharedPreferences == null ){
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ContextUtil.getContext());
		}
		return sharedPreferences;
	}
	@Override
    public void writeToParcel(Parcel dest, int flags){

		Parcel obtain = Parcel.obtain();
		//写入数据
		writeToParcel(obtain);

		compression(obtain, dest);
    }

	private void compression(Parcel obtain, Parcel dest) throws Error{
		boolean data_compression_enable = getSharedPreferences().getBoolean("data_compression_enable", false);
		int data_compression_threshold = 25;
		try{
			data_compression_threshold = Integer.parseInt(getSharedPreferences().getString("data_compression_threshold", "25"));
		}
		catch (NumberFormatException e){}
		int data_compression_level = Deflater.DEFLATED;
		try{
			data_compression_level = Integer.parseInt(getSharedPreferences().getString("data_compression_level", "9"));
		}
		catch (NumberFormatException e){}
		if ( data_compression_level < 0 || data_compression_level > 9 ){
			data_compression_level = Deflater.DEFLATED;
		}
		//此处1000作为1KB
		compress = data_compression_enable && obtain.dataSize() > data_compression_threshold * 1000;
		//是否压缩标识
		dest.writeInt(compress ? 1 : 0);
		if ( compress ){
			try{
				//压缩数据
				byte[] data = compressParcelData(obtain, data_compression_level);
				dest.writeByteArray(data, 0, data.length);
			}
			catch (Throwable e){
				throw new Error(e);
			}
		}
		else{
			//System.out.println("非＊压缩模式");
			dest.appendFrom(obtain, 0, obtain.dataSize());
		}
		//释放
		obtain.recycle();
	}

	private byte[] compressParcelData(Parcel obtain, final int level) throws IOException{
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		GZIPOutputStream zipParcelOut = new GZIPOutputStream(byteArrayOut){{this.def.setLevel(level);}};
		
		//序列化
		byte[] marshall = obtain.marshall();
		//压缩
		zipParcelOut.write(marshall);
		zipParcelOut.close();
		

		//写入数据
		byte[] data = byteArrayOut.toByteArray();
		
		byteArrayOut.close();
		
		return data;
	}

	private void writeToParcel(Parcel parcel){
		parcel.writeString(this.projectName);
		parcel.writeString(this.mb);
		parcel.writeString(this.jw);
		parcel.writeInt(this.k2 ? 1 : 0);
		parcel.writeString(this.zh);
		parcel.writeString(this.AL);
		parcel.writeString(this.w9);
		parcel.writeString(this.hK);

		parcel.writeInt(this.cT ? 1 : 0);
		parcel.writeInt(this.q7 ? 1 : 0);
		parcel.writeInt(this.Z1 ? 1 : 0);

		parcel.writeInt(this.n5 ? 1 : 0);
		parcel.writeString(this.Q6);
		parcel.writeList(this.qp);
		parcel.writeList(this.kf);
		parcel.writeList(this.Jl);
		parcel.writeList(this.iW);
		parcel.writeList(this.fY);
	}

    @ey(method = -54924251532196657L)
    public EngineSolutionProject(Parcel dest){

		Parcel readParcel = dest;

		compress = dest.readInt() == 1;
		if ( compress ){
			AppLog.DW("解压模式");
			readParcel = decompression(dest);
		}

		this.projectName = readParcel.readString();
		this.mb = readParcel.readString();
		this.jw = readParcel.readString();
		this.k2 = readParcel.readInt() != 0;
		this.zh = readParcel.readString();
		this.AL = readParcel.readString();
		this.w9 = readParcel.readString();
		this.hK = readParcel.readString();
		this.cT = readParcel.readInt() != 0;
		this.q7 = readParcel.readInt() != 0;
		this.Z1 = readParcel.readInt() != 0;

		boolean z = true;
		if ( readParcel.readInt() == 0 ){
			z = false;
		}
		this.n5 = z;
		this.Q6 = readParcel.readString();
		ArrayList<String> arrayList = new ArrayList<>();
		this.qp = arrayList;
		readParcel.readList(arrayList, getClass().getClassLoader());

		ArrayList<String> arrayList2 = new ArrayList<>();
		this.kf = arrayList2;
		readParcel.readList(arrayList2, getClass().getClassLoader());

		ArrayList<String> arrayList3 = new ArrayList<>();
		this.Jl = arrayList3;
		readParcel.readList(arrayList3, getClass().getClassLoader());

		ArrayList<String> arrayList4 = new ArrayList<>();
		this.iW = arrayList4;
		readParcel.readList(arrayList4, getClass().getClassLoader());

		ArrayList<EngineSolution.File> arrayList5 = new ArrayList<EngineSolution.File>();
		this.fY = arrayList5;
		readParcel.readList(arrayList5, getClass().getClassLoader());
		if ( readParcel != dest ){
			readParcel.recycle();
		}
    }

	private Parcel decompression(Parcel dest){
		try{
			//读取数据长度
			byte[] buf = dest.createByteArray();
			GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(buf));
			byte[] unzipParcelData = gzipInputStream.readAllBytes();
			Parcel obtain = Parcel.obtain();
			obtain.unmarshall(unzipParcelData, 0, unzipParcelData.length);
			obtain.setDataPosition(0);

			//关闭
			gzipInputStream.close();

			return obtain;
		}
		catch (Throwable e){
			Log.d("EngineSolutionProject", "unZipParcel", e);

			throw new Error(e);
		}
	}
}

