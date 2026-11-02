حزمة صفحة نور الإيمان
====================

تحتوي الحزمة على:
- index.html: صفحة العرض العربية المتجاوبة.
- .github/workflows/android.yml: بناء APK ورفعه كـ GitHub Release ثابت.
- .github/workflows/pages.yml: نشر الصفحة عبر GitHub Pages.

بعد فك الضغط داخل مجلد نسخة المستودع:
git add index.html .github/workflows/android.yml .github/workflows/pages.yml
git commit -m "Add Noor Al Iman landing page and stable APK release"
git push origin main

ثم من GitHub:
Settings > Pages > Build and deployment > Source
اختر GitHub Actions.

بعد نجاح التشغيل:
https://asimalimikial-rgb.github.io/Noor-aliman/

رابط APK الثابت بعد نجاح تشغيل Android workflow:
https://github.com/asimalimikial-rgb/Noor-aliman/releases/download/android-latest/noor-aliman.apk

ملاحظة: الملف الناتج Debug APK وليس Release signed APK.
