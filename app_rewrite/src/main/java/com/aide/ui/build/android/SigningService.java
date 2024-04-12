package com.aide.ui.build.android;

import abcd.cy;
import abcd.ed;
import abcd.ey;
import abcd.fy;
import abcd.gy;
import abcd.iy;
import abcd.qq0;
import android.app.Activity;
import com.aide.common.AppLog;
import com.aide.common.MessageBox;
import com.aide.ui.App;
import com.aide.ui.MainActivity;
import com.aide.ui.util.BuildGradle;
import com.aide.ui.util.FileSystem;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import abcd.dy;
import abcd.hy;
import com.aide.common.ValueRunnable;
import com.aide.ui.AppPreferences;

public class SigningService {
    @gy
    private static boolean DW;
    @fy
    private static boolean j6;

    @cy(clazz = -456191370199574592L, container = -456191370199574592L, user = true)
    /* loaded from: /storage/emulated/0/AppProjects1/.ZeroAicy/git/classes.dex */
    public interface SigningRunnable {
        @ey(method = 1826402226779581395L)
        void j6(String str, String str2, String str3, String str4);
    }

    @cy(clazz = -2971104453650556349L, container = 1054312504720486788L, user = true)
    /* loaded from: /storage/emulated/0/AppProjects1/.ZeroAicy/git/classes.dex */
	static class a implements Runnable {
        @fy
        private static boolean fY;
        @gy
        private static boolean qp;
        @dy(field = -1325875345222548792L)
        final String WB;
        @dy(field = -2436604586145215409L)
        @hy
        final SigningService jw;
        @dy(field = 3871165034209308000L)
        final SigningRunnable mb;

        @ey(method = -3441480203686840587L)
        a(SigningService signingService, String str, SigningRunnable signingRunnable) {
            this.jw = signingService;
            this.WB = str;
            this.mb = signingRunnable;
        }

        @Override // java.lang.Runnable
        @ey(method = -962677808293196125L)
        public void run() {
            try {
                if (fY) {
                    iy.gn(472526088977022796L, this);
                }
                this.jw.v5(this.WB, this.mb);
            }
			catch (Throwable th) {
                if (qp) {
                    iy.aM(th, 472526088977022796L, this);
                }
                throw new Error(th);
            }
        }
    }

    public static class b implements ValueRunnable<String> {
        @gy
        private static boolean Zo;
        @fy
        private static boolean v5;
        @dy(field = 4401127368425169043L)
        final String DW;
        @dy(field = 3592669819486048336L)
        final SigningRunnable FH;
        @dy(field = -564731930029743215L)
        @hy
        final SigningService Hw;
        @dy(field = -5640335584373944044L)
        final String j6;

        static {
            iy.Zo(b.class);
        }

        @ey(method = 17269374959139957L)
        b(SigningService signingService, String str, String str2, SigningRunnable signingRunnable) {
            this.Hw = signingService;
            this.j6 = str;
            this.DW = str2;
            this.FH = signingRunnable;
        }

        @ey(method = -8482063888119037329L)
        public void DW(String str) {
            try {
                if (v5) {
                    iy.tp(7039568769271022775L, this, str);
                }
                SigningService.j6(this.Hw, this.j6, this.DW, str, this.FH);
            }
			catch (Throwable th) {
                if (Zo) {
                    iy.j3(th, 7039568769271022775L, this, str);
                }
                throw new Error(th);
            }
        }

        @ey(method = 5003894405963009304L)
        public /* bridge */ void j6(String obj) {
            DW(obj);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @cy(clazz = -2971311645518541765L, container = 1054312504720486788L, user = true)
    /* loaded from: /storage/emulated/0/AppProjects1/.ZeroAicy/git/classes.dex */
    public static class c implements ValueRunnable<String> {
        @gy
        private static boolean VH;
        @fy
        private static boolean Zo;
        @dy(field = 2320447443640381865L)
        final String DW;
        @dy(field = -3408984433179110808L)
        final String FH;
        @dy(field = -3343611760975335240L)
        final SigningRunnable Hw;
        @dy(field = 1796514412372031136L)
        final String j6;
        @dy(field = 2384576196773045535L)
        @hy
        final SigningService v5;

        static {
            iy.Zo(c.class);
        }

        @ey(method = -196320373074543L)
        c(SigningService signingService, String str, String str2, String str3, SigningRunnable signingRunnable) {
            this.v5 = signingService;
            this.j6 = str;
            this.DW = str2;
            this.FH = str3;
            this.Hw = signingRunnable;
        }

        @ey(method = -3768758292758685135L)
        public void DW(String str) {
            try {
                if (Zo) {
                    iy.tp(-1142975430791632256L, this, str);
                }
                SigningService.DW(this.v5, this.j6, this.DW, this.FH, str, this.Hw);
            }
			catch (Throwable th) {
                if (VH) {
                    iy.j3(th, -1142975430791632256L, this, str);
                }
                throw new Error(th);
            }
        }

        @ey(method = -7297015113097814044L)
        public /* bridge */ void j6(String obj) {
            DW(obj);
        }
    }

    static class d implements ed.f {
        @fy
        private static boolean DW;
        @gy
        private static boolean FH;
        @dy(field = 1882372525128910500L)
        final Activity j6;


        d(SigningService signingService, Activity activity) {
            this.j6 = activity;
        }

        public void j6(boolean z, String str) {
            try {
                if (DW) {
                    iy.EQ(2875446220319754268L, this, new Boolean(z), str);
                }
                if (z) {
                    AppPreferences.I(str);
                    Activity activity = this.j6;
                    MessageBox.ei(activity, "Create keystore", "Keystore file " + str + " has been created and set as default.", (Runnable) null);
                    return;
                }
                Activity activity2 = this.j6;
                MessageBox.BT(activity2, "Create keystore", "Failed to create keystore file " + str);
            }
			catch (Throwable th) {
                if (FH) {
                    iy.Mr(th, 2875446220319754268L, this, new Boolean(z), str);
                }
                throw new Error(th);
            }
        }
    }

    static {
        iy.Zo(SigningService.class);
    }

    @ey(method = 2173769266671334785L)
    public SigningService() {
        try {
            if (j6) {
                iy.gn(3292782087160200064L, (Object) null);
            }
        }
		catch (Throwable th) {
            if (DW) {
                iy.aM(th, 3292782087160200064L, (Object) null);
            }
            throw new Error(th);
        }
    }

    @ey(method = 5110595402486911839L)
    static void DW(SigningService signingService, String str, String str2, String str3, String str4, SigningRunnable signingRunnable) {
        signingService.FH(str, str2, str3, str4, signingRunnable);
    }

    @ey(method = -765423962236243125L)
    private void FH(String keystorePath, String str2, String str3, String str4, SigningRunnable signingRunnable) {
        try {
            if (j6) {
                iy.J8(-4033616883024997667L, this, keystorePath, str2, str3, str4, signingRunnable);
            }
            try {
				if ( keystorePath.endsWith(".x509.pem") 
					|| keystorePath.endsWith(".pk8") ){
					signingRunnable.j6(keystorePath, str2, str3, str4);
				}
                JKSKeyStore jKSKeyStore = new JKSKeyStore();
                jKSKeyStore.load(new FileInputStream(keystorePath), str2.toCharArray());
                if (jKSKeyStore.getKey(str3, str4.toCharArray()) != null) {
                    signingRunnable.j6(keystorePath, str2, str3, str4);
                    return;
                }
                throw new Exception("no alias");
            }
			catch (Exception unused) {
                MessageBox.BT(App.getMainActivity(), "Build Error", "Invalid keystore credentials!");
            }
        }
		catch (Throwable th) {
            if (DW) {
                iy.lg(th, -4033616883024997667L, this, keystorePath, str2, str3, str4, signingRunnable);
            }
            throw new Error(th);
        }
    }

    @ey(method = 4111598620218933911L)
    private void VH(String keystorePath, String str2, SigningRunnable signingRunnable) {
        try {
            if (j6) {
                iy.we(3682206307279491105L, this, keystorePath, str2, signingRunnable);
            }
            try {
				if ( keystorePath.endsWith(".x509.pem") 
					|| keystorePath.endsWith(".pk8") ){
					signingRunnable.j6(keystorePath, "", "", "");
				}
                JKSKeyStore jKSKeyStore = new JKSKeyStore();
                jKSKeyStore.load(new FileInputStream(keystorePath), str2.toCharArray());
                ArrayList list = Collections.list(jKSKeyStore.aliases());
                if (list.size() == 1) {
                    gn(keystorePath, str2, (String) list.get(0), signingRunnable);
                } else {
                    MessageBox.VH(App.getMainActivity(), "Select keystore alias", list, new b(this, keystorePath, str2, signingRunnable));
                }
            }
			catch (Exception unused) {
                MessageBox.BT(App.getMainActivity(), "Build Error", "Invalid keystore credentials!");
            }
        }
		catch (Throwable th) {
            if (DW) {
                iy.U2(th, 3682206307279491105L, this, keystorePath, str2, signingRunnable);
            }
            throw new Error(th);
        }
    }

    @ey(method = -4586519056902629363L)
    private void gn(String str, String str2, String str3, SigningRunnable signingRunnable) {
        try {
            if (j6) {
                iy.J0(5616900279404352443L, this, str, str2, str3, signingRunnable);
            }
            MainActivity mainActivity = App.getMainActivity();
            MessageBox.J8(mainActivity, (String) null, "Enter password for keystore alias '" + str3 + "':", new c(this, str, str2, str3, signingRunnable));
        }
		catch (Throwable th) {
            if (DW) {
                iy.a8(th, 5616900279404352443L, this, str, str2, str3, signingRunnable);
            }
            throw new Error(th);
        }
    }

    @ey(method = -56070401644590480L)
    static void j6(SigningService signingService, String str, String str2, String str3, SigningRunnable signingRunnable) {
        signingService.gn(str, str2, str3, signingRunnable);
    }

	public boolean Hw(String str, String str2, String str3, String str4, Date date, Date date2, BigInteger bigInteger, String str5, String str6, String str7, String str8, String str9, String str10) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(1024);
            KeyPair generateKeyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = generateKeyPair.getPublic();
            PrivateKey privateKey = generateKeyPair.getPrivate();
            qq0 qq0Var = new qq0();
            String str13 = "CN=" + str5;
            if (str6.length() > 0) {
                str13 = str13 + ", O=" + str6;
            }
            if (str7.length() > 0) {
                str13 = str13 + ", OU=" + str7;
            }
            if (str8.length() > 0) {
                str13 = str13 + ", L=" + str8;
            }
            if (str9.length() > 0) {
                str13 = str13 + ", ST=" + str9;
            }
            if (str10.length() > 0) {
                str13 = str13 + ", C=" + str10;
            }
            X500Principal x500Principal = new X500Principal(str13);
            qq0Var.u7(bigInteger);
            qq0Var.v5(x500Principal);
            qq0Var.EQ(x500Principal);
            qq0Var.VH(date);
            qq0Var.Zo(date2);
            qq0Var.gn(publicKey);
            qq0Var.tp("SHA256WithRSAEncryption");
            X509Certificate j6 = qq0Var.j6(privateKey, "BC");
            JKSKeyStore jKSKeyStore = new JKSKeyStore();
            jKSKeyStore.load(null, null);
			jKSKeyStore.setKeyEntry(str3, privateKey, str4.toCharArray(), new Certificate[]{j6});
			
			jKSKeyStore.store(new FileOutputStream(str), str2.toCharArray());
			return true;
			

        }
		catch (Throwable th) {
			throw new Error(th);
        }

    }

    @ey(method = 374861035471152168L)
    public void Zo(String str, BuildGradle.SigningConfig signingConfig, SigningRunnable signingRunnable) {
        try {
            if (j6) {
                iy.we(759067435658049504L, this, str, signingConfig, signingRunnable);
            }
            if (signingConfig != null) {
                try {
                    String storeFilePath = signingConfig.getStoreFilePath();
					if ( storeFilePath.endsWith(".x509.pem") 
						|| storeFilePath.endsWith(".pk8") ){
						signingRunnable.j6(storeFilePath, signingConfig.storePassword, signingConfig.keyAlias, signingConfig.keyPassword);
					}
                    JKSKeyStore jKSKeyStore = new JKSKeyStore();
                    jKSKeyStore.load(new FileInputStream(storeFilePath), signingConfig.storePassword.toCharArray());
                    if (jKSKeyStore.getKey(signingConfig.keyAlias, signingConfig.keyPassword.toCharArray()) != null) {
                        signingRunnable.j6(storeFilePath, signingConfig.storePassword, signingConfig.keyAlias, signingConfig.keyPassword);
                        return;
                    }
                    throw new Exception("can not read keystore");
                }
				catch (Exception unused) {
                    MessageBox.rN(App.getMainActivity(), "Build Error", "Failed to open signingConfig from build.gradle. Use alternative signing?", new a(this, str, signingRunnable), (Runnable) null);
                    return;
                }
            }
            v5(str, signingRunnable);
        }
		catch (Throwable th) {
            if (DW) {
                iy.U2(th, 759067435658049504L, this, str, signingConfig, signingRunnable);
            }
            throw new Error(th);
        }
    }

    @ey(method = 402808675949028321L)
    public void u7(Activity activity) {
        try {
            if (j6) {
                iy.tp(1800541819170950631L, this, activity);
            }
            MessageBox.gW(activity, new ed(FileSystem.vy() + "/AppProjects/debug.keystore", "androiddebugkey", "android", new d(this, activity)));
        }
		catch (Throwable th) {
            if (DW) {
                iy.j3(th, 1800541819170950631L, this, activity);
            }
            throw new Error(th);
        }
    }

    @ey(method = 6458267827521734465L)
    public void v5(String str, SigningRunnable signingRunnable) {
        try {
            if (j6) {
                iy.EQ(30499914356608167L, this, str, signingRunnable);
            }
            if (str != null && str.length() > 0) {
                if (FileSystem.isFileAndNotZip(str)) {
                    try {
                        JKSKeyStore jKSKeyStore = new JKSKeyStore();
                        jKSKeyStore.load(new FileInputStream(str), "android".toCharArray());
                        if (jKSKeyStore.getKey("androiddebugkey", "android".toCharArray()) != null) {
                            signingRunnable.j6(str, "android", "androiddebugkey", "android");
                            return;
                        }
                        throw new Exception("no androiddebugkey");
                    }
					catch (Exception unused) {
                        if (App.a8().VH(App.gn(), "custom_keystore")) {
                            VH(str, "", signingRunnable);
                            return;
                        }
                        return;
                    }
                }
                MainActivity mainActivity = App.getMainActivity();
                MessageBox.BT(mainActivity, "Build Error", "Keystore file " + str + " does not exist!");
                return;
            }
            signingRunnable.j6("", "", "", "");
        }
		catch (Throwable th) {
            if (DW) {
                iy.Mr(th, 30499914356608167L, this, str, signingRunnable);
            }
            throw new Error(th);
        }
    }
}

