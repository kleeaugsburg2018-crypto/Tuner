#!/usr/bin/env python3
"""Build and sign using official Android SDK build tools; no network at runtime."""
import os, pathlib, subprocess, secrets, shutil, zipfile
root=pathlib.Path(__file__).resolve().parent
sdk=pathlib.Path(os.environ.get('ANDROID_TOOLCHAIN','/workspace/android-toolchain'))
tools=sdk/'tools/android-15'; android=sdk/'platform/android-35/android.jar'
build=root/'build';build.mkdir(exist_ok=True);(build/'classes').mkdir(exist_ok=True);(build/'dex').mkdir(exist_ok=True)
def run(*args): subprocess.run([str(a) for a in args],check=True,cwd=root)
run(tools/'aapt2','compile','--dir',root/'app/src/main/res','-o',build/'resources.zip')
run(tools/'aapt2','link','-o',build/'unsigned.apk','-I',android,'--manifest',root/'app/src/main/AndroidManifest.xml','-A',root/'app/src/main/assets',build/'resources.zip')
run('java','-jar',sdk/'ecj.jar','-1.8','-bootclasspath',android,'-d',build/'classes',root/'app/src/main/java/org/youthorchestra/tuner/MainActivity.java')
with zipfile.ZipFile(build/'classes.jar','w',zipfile.ZIP_DEFLATED) as classes:
 for p in (build/'classes').rglob('*.class'): classes.write(p,p.relative_to(build/'classes'))
run('java','-cp',tools/'lib/d8.jar','com.android.tools.r8.R8','--release','--min-api','26','--lib',android,'--pg-conf',root/'proguard-rules.pro','--output',build/'dex',build/'classes.jar')
with zipfile.ZipFile(build/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as apk:
 for p in (build/'dex').glob('*.dex'): apk.write(p,p.name)
run(tools/'zipalign','-f','-p','4',build/'unsigned.apk',build/'aligned.apk')
sign=root/'signing';sign.mkdir(exist_ok=True);os.chmod(sign,0o700)
password=sign/'password.txt'
if not password.exists():password.write_text(secrets.token_urlsafe(32));os.chmod(password,0o600)
env=os.environ.copy();env['YO_KEY_PASSWORD']=password.read_text().strip()
if not (sign/'release.jks').exists():
 subprocess.run(['keytool','-genkeypair','-keystore',str(sign/'release.jks'),'-storepass:env','YO_KEY_PASSWORD','-keypass:env','YO_KEY_PASSWORD','-alias','youth-orchestra','-keyalg','RSA','-keysize','3072','-validity','10000','-dname','CN=Youth Orchestra, OU=Music Education'],check=True,env=env)
out=root/'Youth-Orchestra-1.0.1.apk'
subprocess.run([str(tools/'apksigner'),'sign','--ks',str(sign/'release.jks'),'--ks-key-alias','youth-orchestra','--ks-pass','env:YO_KEY_PASSWORD','--out',str(out),str(build/'aligned.apk')],check=True,env=env)
run(tools/'apksigner','verify','--verbose',out)
print(out)
