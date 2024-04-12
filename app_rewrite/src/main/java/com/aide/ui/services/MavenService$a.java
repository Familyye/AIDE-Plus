//
// Decompiled by Jadx - 1587ms
//
package com.aide.ui.services;

import abcd.cy;
import abcd.dy;
import abcd.ey;
import abcd.fy;
import abcd.gy;
import abcd.hy;
import abcd.iy;
import com.aide.ui.util.FileSystem;
import java.io.IOException;

class MavenService$a implements Runnable {
    final MavenService WB;


    public MavenService$a(MavenService mavenService) {
        this.WB = mavenService;
    }

    @Override
    public void run() {
        try {
            try {
				this.WB.resetDepPathMap();
				//删除
                FileSystem.VH(MavenService.getDefaulRepositoriePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Throwable th) {
			throw new Error(th);
        }
    }
}

