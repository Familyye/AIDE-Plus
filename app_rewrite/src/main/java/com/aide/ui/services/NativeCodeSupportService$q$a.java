//
// Decompiled by Jadx - 645ms
//
package com.aide.ui.services;

import abcd.cy;
import abcd.dy;
import abcd.ey;
import abcd.fy;
import abcd.gy;
import abcd.iy;
import com.aide.ui.services.NativeCodeSupportService;
import com.aide.ui.build.packagingservice.ExternalPackagingService;

class NativeCodeSupportService$q$a implements Runnable {
    final boolean WB;
    final NativeCodeSupportService$q mb;
    public NativeCodeSupportService$q$a(NativeCodeSupportService$q qVar, boolean z) {
        this.mb = qVar;
        this.WB = z;ExternalPackagingService m;
    }

    @Override
    public void run() {
        try {
            NativeCodeSupportService.FH(this.mb.v5);
            if (this.WB) {
                NativeCodeSupportService$q.j6(this.mb).run();
            }
        } catch (Throwable th) {
           throw new Error(th);
        }
    }
}
