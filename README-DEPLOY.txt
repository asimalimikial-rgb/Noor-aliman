نور الإيمان — دليل البناء والنشر
================================

المشروع Android أصلي بواجهة WebView. مجلد التطبيق هو `App/` (بحرف A كبير).

متطلبات البناء:
- JDK متوافق مع Gradle 8.7.3 (يوصى بـ JDK 17).
- Android SDK مع Android Platform 36 و Build Tools المتوافقة مع AGP 8.13.2.
- اتصال إنترنت أول مرة لتنزيل Gradle/Android dependencies.

بناء نسخة Debug:
  ./gradlew :app:assembleDebug

المخرج:
  App/build/outputs/apk/debug/app-debug.apk

بناء نسخة Release:
- لا تضع ملف keystore أو كلمات مروره في المستودع.
- عرّف المتغيرات البيئية التالية في بيئة بناء آمنة:
  NOOR_ALIMAN_KEYSTORE
  NOOR_ALIMAN_STORE_PASSWORD
  NOOR_ALIMAN_KEY_PASSWORD
- اسم المفتاح المتوقع: `noor-aliman`.
  ./gradlew :app:assembleRelease

المخرج:
  App/build/outputs/apk/release/app-release.apk

ملاحظات مهمة:
- لا ترفع APK جديداً قبل اختبار التثبيت والترقية فوق النسخة السابقة.
- يجب الحفاظ على نفس مفتاح التوقيع عند تحديث التطبيق للمستخدمين الحاليين.
- Full-screen intent يخضع لإعدادات Android وسياسات النظام؛ عند عدم السماح به
  سيظل إشعار الأذان العادي متاحاً.
- يستهدف هذا المصدر Android 16 (API 36) باستخدام Android Gradle Plugin 8.13.2
  وGradle 8.13. يلزم تثبيت Android Platform 36 في بيئة البناء.
- قبل النشر، اختبر سلوك Edge-to-edge وExact Alarms وFull-screen Intent على Android 14–16.
  شاشة الأذان عند القفل تعتمد على صلاحية Android وسياسات متجر التوزيع، وليست مضمونة.

