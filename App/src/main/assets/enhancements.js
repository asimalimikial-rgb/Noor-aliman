(() => {
  const $ = id => document.getElementById(id);
  // A local Northern Sudan preset avoids silently using Khartoum coordinates for Merowe/Dibeiba.
  if (typeof CITIES !== 'undefined' && !CITIES.dubeiba) {
    CITIES.dubeiba = {
      n: 'الدبيبة، مروي، الولاية الشمالية',
      lat: 18.45352, lng: 31.81315, tz: 'Africa/Khartoum', m: 'umm'
    };
    document.querySelectorAll('.city-select,#settingCity').forEach(select => {
      if (![...select.options].some(option => option.value === 'dubeiba')) {
        const option = document.createElement('option');
        option.value = 'dubeiba';
        option.textContent = CITIES.dubeiba.n;
        select.append(option);
      }
    });
  }
  const ALZAIN = 'ar.alzainmohamedahmed';
  const ABDULKAREEM = 'ar.muhammadabdulkareem';
  const EN = {
    'نور الإيمان': 'Noor Al-Iman', 'رفيق يومك إلى الطمأنينة': 'A companion for your day',
    'القائمة الرئيسية': 'Main menu', 'الرئيسية': 'Home', 'اتجاه القبلة': 'Qibla direction',
    'القبلة': 'Qibla', 'المصحف المرتل': 'Quran', 'المصحف': 'Quran', 'الأذكار': 'Adhkar',
    'المسبحة': 'Tasbeeh', 'الإعدادات': 'Settings', 'حول التطبيق': 'About',
    'السلام عليكم،': 'Peace be upon you,', 'أهلًا بك': 'Welcome',
    'اجعل من يومك مساحةً للذكر والسكينة.': 'Make space in your day for remembrance and tranquility.',
    'يومٌ مبارك': 'A blessed day', 'مواقيت الصلاة': 'Prayer times', 'الصلاة القادمة': 'Next prayer',
    'الفجر': 'Fajr', 'الشروق': 'Sunrise', 'الظهر': 'Dhuhr', 'العصر': 'Asr',
    'المغرب': 'Maghrib', 'العشاء': 'Isha', 'نسأل الله أن يتقبل منكم صالح الأعمال': 'May Allah accept your good deeds',
    'الوقت المتبقي للصلاة': 'Time until prayer', 'صباحًا · القادمة': 'Morning · next',
    'صباحًا': 'Morning', 'ظهرًا': 'Noon', 'عصرًا': 'Afternoon', 'مساءً': 'Evening',
    'مساحتك اليومية': 'Your daily space', 'تخصيص تجربتك ←': 'Personalize your experience →',
    'موقعك الحالي': 'Your location', 'تغيير': 'Change', 'وجهتك إلى البيت الحرام': 'Your direction to the Sacred House',
    'الخرطوم، السودان': 'Khartoum, Sudan', 'الدبيبة، مروي، الولاية الشمالية': 'Ad Dubaybah, Merowe, Northern State',
    'مكة المكرمة، السعودية': 'Makkah, Saudi Arabia',
    'القاهرة، مصر': 'Cairo, Egypt', 'إسطنبول، تركيا': 'Istanbul, Turkey', 'موقعي الحالي': 'My location',
    '● محسوب فلكيًا': '● Calculated astronomically', 'شمال': 'North', 'شمال شرقي': 'Northeast',
    'شرق': 'East', 'جنوب شرقي': 'Southeast', 'جنوب': 'South', 'جنوب غربي': 'Southwest',
    'غرب': 'West', 'شمال غربي': 'Northwest', 'من موقعك الحالي': 'From your location',
    'اتجه نحو السهم للوصول إلى القبلة.': 'Follow the arrow to face the Qibla.',
    'حرّك الهاتف بشكل الرقم ٨ لمعايرة البوصلة عند الحاجة.': 'Move the phone in a figure-eight to calibrate the compass if needed.',
    'البوصلة غير مفعّلة': 'Compass is off', 'تفعيل البوصلة': 'Enable compass',
    'زاوية القبلة من الشمال الحقيقي': 'Qibla bearing from true north',
    'علامة الشمال تشير إلى الشمال الحقيقي؛ والسهم الذهبي يشير إلى القبلة.': 'North marks true north; the gold arrow points to the Qibla.',
    'البوصلة متوقفة حسب الإعداد؛ تبقى زاوية القبلة محسوبة.': 'Compass is off by your setting; the Qibla bearing is still calculated.',
    'عرض النص القرآني عبر': 'Quran text:',
    '. تسجيلات مشاري العفاسي والسديس من Quran.com، وماهر المعيقلي ومحمد عبدالكريم من': '. Mishary Alafasy and Abdurrahman Al-Sudais recordings are from Quran.com; Maher Al-Muaiqly and Mohamed AbdulKareem recordings are from',
    '. تسجيلات الزين محمد أحمد ونورين محمد صديق آيةً آيةً عبر': '. Al-Zain Mohamed Ahmed and Noreen Mohamed Siddiq are available verse by verse via',
    '، ضمن': ', included in',
    '(ترخيص مجموعة البيانات: Apache-2.0). تبقى حقوق التسجيلات لأصحابها ومزوديها وتخضع لشروطهم.': '(dataset license: Apache-2.0). Recording rights remain with their owners and providers and are subject to their terms.',
    'عرض القبلة بالكاميرا': 'View Qibla through camera',
    'تعمل الكاميرا بعد إذنك فقط، ويمكن إغلاقها في أي وقت.': 'The camera works only with your permission and can be closed at any time.',
    'الزاوية محسوبة من الشمال الحقيقي بالدائرة العظمى إلى الكعبة.': 'The angle is calculated from true north along the great-circle route to the Kaaba.',
    'اتبع السهم نحو القبلة': 'Follow the arrow toward the Qibla', 'جارٍ فتح الكاميرا...': 'Opening camera…',
    'إغلاق ×': 'Close ×', 'حرّك الهاتف ببطء حتى يستقر السهم باتجاه القبلة': 'Move the phone slowly until the arrow settles toward the Qibla',
    'تلاوة وطمأنينة': 'Recitation and tranquility', 'مصحف المدينة المنورة': 'Madinah Mushaf',
    'مشاري العفاسي': 'Mishary Alafasy', 'عبدالرحمن السديس': 'Abdurrahman Al-Sudais',
    'ماهر المعيقلي': 'Maher Al-Muaiqly', 'محمد عبد الكريم · سوداني': 'Mohamed AbdulKareem · Sudanese',
    'الزين محمد أحمد · سوداني': 'Al-Zain Mohamed Ahmed · Sudanese',
    'نورين محمد صديق · سوداني': 'Noreen Mohamed Siddiq · Sudanese',
    '⬇ تنزيل بدون إنترنت': '⬇ Download for offline use', 'السابق': 'Previous', 'السابقة': 'Previous',
    'التالي': 'Next', 'التالية': 'Next', 'اذهب': 'Go', 'صفحة': 'Page', 'من ٦٠٤': 'of 604',
    'الصفحة ١ · ٧ آية': 'Page 1 · 7 verses', 'تكرار ×١': 'Repeat ×1', '×٢': '×2', '×٣': '×3', '×٥': '×5',
    'اضغط تشغيل أو المس أي آية للاستماع.': 'Press Play or tap any verse to listen.',
    'إظهار التفسير ومعاني الكلمات': 'Show tafsir and word meanings',
    'يعمل التفسيران دون اتصال بالإنترنت.': 'Both tafsirs work offline.',
    'يعمل التفسير المحدد دون اتصال بالإنترنت.': 'The selected tafsir works offline.',
    'اختر التفسير': 'Choose a tafsir', 'التفسير': 'Tafsir', 'تفسير السعدي': 'Tafsir As-Saadi',
    'تعذّر تحميل التفسير المحلي.': 'Could not load the offline tafsir.',
    'لم يرد تفسير مستقل لهذه الآية في المصدر.': 'The source has no separate explanation for this verse.',
    'تفسير المختصر': 'Al-Mukhtasar tafsir', 'السراج في بيان غريب القرآن': 'Al-Siraj: Quranic vocabulary',
    'من بعد صلاة العصر حتى المغرب': 'After Asr until Maghrib', 'أذكار المساء': 'Evening adhkar',
    '☀ أذكار الصباح': '☀ Morning adhkar', '☾ أذكار المساء': '☾ Evening adhkar',
    'إعادة ضبط ↺': 'Reset ↺', 'اضغط للعد': 'Tap to count', 'اذكر الله في كل حين': 'Remember Allah at all times',
    'المسبحة الإلكترونية': 'Digital tasbeeh', 'تسبيح': 'Count', 'إعادة ضبط العدّاد ↺': 'Reset counter ↺',
    'كما تحبّها': 'Your preferences', 'المدينة': 'City', 'الموقع المستخدم لحساب المواقيت والقبلة': 'Location used for prayer times and Qibla',
    'طريقة الحساب': 'Calculation method', '«تلقائي» يختار الطريقة الأنسب للمدينة': '“Automatic” chooses a suitable method for your city',
    'تلقائي': 'Automatic', 'رابطة العالم الإسلامي': 'Muslim World League', 'أم القرى': 'Umm Al-Qura',
    'الهيئة المصرية العامة للمساحة': 'Egyptian General Authority of Survey',
    'جامعة العلوم الإسلامية بكراتشي': 'University of Islamic Sciences, Karachi',
    'المذهب للعصر': 'Asr juristic method', 'طريقة حساب وقت صلاة العصر': 'Method for calculating Asr time',
    'الجمهور (ظل المثل)': 'Standard (single shadow)', 'حنفي (ظل المثلين)': 'Hanafi (double shadow)',
    'التسبيح الدوري': 'Timed dhikr', 'صوت ذكر يتكرر على فترات تختارها': 'Play a dhikr at an interval you choose',
    'التكرار': 'Interval', 'متوقف': 'Off',
    'كل ٥ دقائق': 'Every 5 minutes', 'كل ١٠ دقائق': 'Every 10 minutes', 'كل ربع ساعة': 'Every 15 minutes',
    'كل ثلث ساعة': 'Every 20 minutes', 'كل نصف ساعة': 'Every 30 minutes', 'كل ساعة': 'Every hour',
    'كل ساعتين': 'Every 2 hours', 'الذكر': 'Dhikr',
    'الأذكار مسجلة محليًا؛ والصلاة على النبي لها صوت مدمج يعمل دون إنترنت': 'Dhikr and salawat audio are included and work offline',
    'سبحان الله': 'Glory be to Allah', 'الحمد لله': 'Praise be to Allah', 'الله أكبر': 'Allah is greatest',
    'صلوا على رسول الله': 'Send blessings upon the Messenger', 'من الساعة / إلى الساعة': 'From / until',
    'لا يعمل خارج هذه الفترة (مثلًا وقت النوم)': 'Inactive outside this period (for example, while sleeping)',
    'مستوى الصوت': 'Volume', 'اضغط تجربة لتسمع': 'Press Test to hear it', 'تجربة': 'Test',
    'تنبيهات الصلاة': 'Prayer alerts', 'صوت الأذان كاملًا عبر تنبيهات أندرويد، حتى عند إغلاق التطبيق': 'Full adhan through Android notifications, even when the app is closed',
    'تذكير قبل الصلاة': 'Prayer reminder', 'تنبيه أندرويد قبل كل صلاة بعشر دقائق': 'Android alert 10 minutes before each prayer',
    'صوت الأذان': 'Adhan sound', 'اختر صوتًا محفوظًا محليًا وجربه دون إنترنت': 'Choose and preview an audio file stored on this device',
    'أذان كامل · ٣:٠٣': 'Full adhan · 3:03', 'أذان عقيب عزيز · ١:٢٧': 'Aaqib Aziz adhan · 1:27',
    'أذان مختصر · ٠:٤٢': 'Short adhan · 0:42',
    'أذان أزمور، المغرب · ٢:٤٣': 'Azemmour, Morocco adhan · 2:43',
    'متابعة التلاوة للصفحة التالية': 'Continue recitation to the next page',
    'وضع المصحف': 'Mushaf mode', 'قراءة فقط': 'Reading only', 'قراءة مع التلاوة': 'Reading with recitation',
    'الصفحة السابقة': 'Previous page', 'الصفحة التالية': 'Next page',
    'اسحب الصفحة أو استخدم زري التنقل بجانب المصحف.': 'Swipe the page or use the nearby page-turn buttons.',
    'مصادر تسجيلات الأذان': 'Adhan recording sources',
    'الانتقال تلقائيًا دون توقف': 'Continue automatically without pausing', 'الوضع الليلي': 'Dark mode',
    'مظهر داكن مريح للعين': 'A dark theme that is easy on the eyes', 'اللغة': 'Language',
    'الواجهة عربية حاليًا': 'Choose the interface language', 'العربية': 'Arabic', 'English (قريبًا)': 'English',
    'تشغيل تلاوة القرآن': 'Enable Quran recitation',
    'إظهار أدوات الصوت وتشغيل التلاوة عند الطلب': 'Show audio controls and play recitation on request',
    'عن التطبيق والمصادر ←': 'About and sources →',
    'تسجيلات الأذان محفوظة داخل التطبيق وتعمل دون إنترنت. الحقوق:': 'Adhan recordings are included and work offline. Credits:',
    'معلومات ومصادر': 'Information and sources', 'عن التطبيق': 'About the app', 'الخصوصية والعمل دون اتصال': 'Privacy and offline use',
    'مصادر أصوات الأذكار': 'Dhikr audio sources', 'مصادر القرآن والتلاوات': 'Quran text and recitation sources',
    'التنبيهات': 'Alerts', 'المحمَّل على جهازك': 'Downloaded to your device',
    'تنزيل للاستماع والقراءة بدون إنترنت': 'Download for offline listening and reading',
    'القارئ': 'Reciter', 'السورة': 'Surah', 'تنزيل السورة': 'Download surah',
    'تنزيل المصحف كاملًا': 'Download all surahs', 'إيقاف التنزيل': 'Stop download',
    'لا توجد تنزيلات بعد.': 'No downloads yet.', 'حذف الكل': 'Delete all',
    'يُعرض اتجاه القبلة من الشمال الحقيقي؛ فعّل المستشعر لعرض اتجاه الهاتف.': 'Qibla direction is shown from true north. Enable the compass to follow the phone direction.',
    'تعذّر تشغيل الصوت. تحقّق من الاتصال ثم اضغط تشغيل.': 'Audio could not start. Check your connection and press Play again.',
    'تعذّر تحميل الصوت؛ تحقّق من الاتصال أو اختر قارئًا آخر.': 'Audio could not load. Check your connection or choose another reciter.',
    'تم تحديد موقعك': 'Your location was found', 'تحديد الموقع غير مدعوم': 'Location is not supported',
    'تعذّر تحديد الموقع، تأكد من الصلاحية': 'Could not find your location. Check the permission.',
    'تمت إعادة ضبط العدّاد': 'Counter reset', 'أحسنت، أتممت ٣٣ تسبيحة': 'Well done, you completed 33 counts',
    'هذا التطبيق وقفٌ لروح المرحومة عزيزة القنديل علي، نرجو الدعاء لها.': 'This app is an endowment in memory of the late Aziza Al-Qandil Ali. Please remember her in your prayers.',
    'وقفٌ لروح المرحومة عزيزة القنديل علي': 'An endowment in memory of the late Aziza Al-Qandil Ali',
    'نرجو الدعاء لها': 'Please remember her in your prayers', 'دعاء وصدقة جارية': 'A prayer and ongoing charity',
    'متابعة': 'Continue', 'إيقاف البوصلة': 'Turn off compass', 'بوصلة الهاتف': 'Phone compass',
    'المواقيت محسوبة فلكيًا على جهازك دون إنترنت، وقد تختلف دقيقة عن التقويم الرسمي لمدينتك.': 'Prayer times are calculated on your device without internet and may differ by a minute from your city’s official timetable.',
    'النصوص من': 'Text from', '، كما وردت من المصدر. المختصر والسراج، الإصدار 1.0.0.': ', as provided by the source. Al-Mukhtasar and Al-Siraj, version 1.0.0.',
    'تفسير السعدي - تيسير الكريم الرحمن، الإصدار 1.0.0، من QuranEnc.': 'Tafsir As-Saadi, Taysir al-Karim ar-Rahman, version 1.0.0, from QuranEnc.',
    'مصدر النصوص:': 'Text source:', '· التفسير المختصر والسراج في بيان غريب القرآن': '· Al-Mukhtasar and Al-Siraj tafsirs',
    'تُعرض الآيات صفحةً صفحةً وفق ترقيم مصحف المدينة (604 صفحات). لتقرأ وتسمع بدون إنترنت، نزّل السورة من علامة ⬇ بجانبها أو من زر «تنزيل بدون إنترنت».': 'Verses are shown page by page using the Madinah Mushaf numbering (604 pages). To read and listen offline, download a surah using the ⬇ icon or the “Download for offline use” button.',
    'أتممت ': 'Completed ',
    'يُصفَّر العدّاد تلقائيًا كل يوم. الأذكار مختارة من الصحيحين والسنن، فراجع أهل العلم عند الحاجة.': 'The counter resets daily. These adhkar are selected from authentic hadith collections; consult a qualified scholar when needed.',
    'تُحفظ إعداداتك تلقائيًا على هذا الجهاز. فعّل أذونات الإشعارات والتنبيهات الدقيقة ليعمل التنبيه عند إغلاق التطبيق.': 'Your settings are saved on this device. Allow notifications and exact alarms so alerts can work when the app is closed.',
    'مقاطع سبحان الله والحمد لله والله أكبر من': 'Recordings of Subhan Allah, Alhamdulillah, and Allahu Akbar from',
    '، بترخيص CC0 1.0. عبارة الصلاة على النبي «صَلُّوا عَلَى رَسُولِ اللَّهِ» ينطقها محرك الصوت العربي المتاح على جهازك عند اختيارها.': ', under CC0 1.0. The salawat phrase is spoken by your device’s Arabic speech engine when selected.',
    'عرض النص القرآني والتلاوات عبر': 'Quran text and recitations:', '، والتلاوات عبر': ' and recitations:',
    '. حقوق التسجيلات لأصحابها ومزوديها، وتخضع لشروطهم.': '. Recordings remain the property of their respective owners and providers and are subject to their terms.',
    'تنبيهات الأذان والتذكير والتسبيح الدوري على أندرويد تحتاج أذونات الإشعارات والتنبيهات الدقيقة. قد يقيّد نظام التشغيل أوقات التنبيه عند تفعيل أوضاع توفير الطاقة.': 'On Android, adhan alerts, reminders, and timed dhikr need notification and exact-alarm permissions. Power-saving modes may delay alerts.',
    'التلاوة': 'Recitation', '0٫8×': '0.8×', '1٫25×': '1.25×', '1٫5×': '1.5×',
    'تعمل فقط عند فتح صفحة القبلة؛ أوقفها لتقليل استخدام المستشعر والبطارية.': 'Runs only while the Qibla page is open. Turn it off to reduce sensor and battery use.',
    'البوصلة متوقفة حسب الإعداد؛ تبقى زاوية القبلة محسوبة.': 'Compass is off by your setting; the Qibla angle is still calculated.',
    'تم إيقاف البوصلة': 'Compass turned off', 'تم تفعيل البوصلة': 'Compass enabled',
    'مشغّل الصوت العربي غير متاح على هذا الجهاز': 'Arabic audio is not available on this device',
    'رفيق يومي لمواقيت الصلاة، واتجاه القبلة، والقرآن الكريم، والأذكار والمسبحة. المواقيت والقبلة تُحسبان على الجهاز، وقد تختلف المواقيت قليلًا عن التقويم الرسمي.': 'A daily companion for prayer times, Qibla, Quran, adhkar, and tasbeeh. Prayer times and Qibla are calculated on this device and may differ slightly from local official timetables.',
    'لا يتطلب التطبيق حسابًا. التفسير ومعاني الكلمات مدمجان ويعملان دون اتصال من أول تشغيل. تُحفظ إعداداتك وموقعك والتنزيلات على هذا الجهاز. يلزم الاتصال لتنزيل صفحات القرآن والتلاوات أول مرة.': 'No account is required. Tafsir and word meanings are built in and work offline. Settings, location, and downloads stay on this device. An internet connection is needed to download Quran pages and recitations the first time.',
    'التنبيهات والأذان والتذكير والتسبيح الدوري على أندرويد تحتاج أذونات الإشعارات والتنبيهات الدقيقة. قد يقيّد نظام التشغيل أوقات التنبيه عند تفعيل أوضاع توفير الطاقة.': 'Android prayer alerts, reminders, and timed dhikr require notification and exact-alarm permissions. Battery-saving modes may delay alerts.',
    'Created by Asim Ali Mikial. لا يدّعي التطبيق ملكية النصوص القرآنية أو تسجيلات التلاوة.': 'Created by Asim Ali Mikial. The app does not claim ownership of Quran text or recitations.',
    'إعدادات الأذان ومواقيت الصلاة': 'Adhan and prayer time settings',
    'التحكم في إشعار الأذان': 'Adhan notification controls',
    'إظهار أدوات التحكم في الإشعار': 'Show notification controls',
    'هذا الخيار لا يوقف صوت الأذان. قد يعرض Android إشعارًا عامًا إلزاميًا أثناء تشغيل الخدمة.': 'This does not stop adhan audio. Android may show a required generic notification while playback is active.',
    'الضبط اليدوي لمواقيت الصلاة': 'Manual prayer time adjustments',
    'التعديل بالدقائق فوق الحساب الأصلي، ويحفظ تلقائيًا.': 'Minute offsets are added to the original calculation and saved automatically.',
    'الأصل': 'Original', 'بعد التعديل': 'Adjusted', 'دقيقة': 'minutes',
    'إعادة ضبط': 'Reset', 'إعادة ضبط جميع التعديلات': 'Reset all adjustments',
    'لا تتوفر في الملفات الحالية تسجيلات من الحرمين بترخيص إعادة توزيع موثق.': 'No recordings from the Two Holy Mosques with verified redistribution rights are included in the current files.',
    'إيقاف الأذان الآن': 'Stop adhan now',
    'تشغيل الأذان في أوقاته': 'Play adhan at prayer times',
    'إيقاف صوت الأذان لا يتوقف عند إخفاء إشعار التحكم.': 'Hiding the control notification does not stop adhan audio.',
    'تشغيل صوت الأذان عند دخول وقت الصلاة، حتى عند إغلاق التطبيق': 'Play adhan at prayer time, including when the app is closed',
    'إنقاص دقيقة من توقيت': 'Decrease the time by one minute',
    'زيادة دقيقة على توقيت': 'Increase the time by one minute'
  };
  const SURAH_EN = [
    'Al-Fatihah','Al-Baqarah',"Ali 'Imran",'An-Nisa','Al-Maidah','Al-Anam',"Al-A'raf",'Al-Anfal','At-Tawbah','Yunus','Hud','Yusuf','Ar-Rad','Ibrahim','Al-Hijr','An-Nahl','Al-Isra','Al-Kahf','Maryam','Ta-Ha','Al-Anbiya','Al-Hajj','Al-Muminun','An-Nur','Al-Furqan',"Ash-Shuara",'An-Naml','Al-Qasas','Al-Ankabut','Ar-Rum','Luqman','As-Sajdah','Al-Ahzab','Saba','Fatir','Ya-Sin','As-Saffat','Sad','Az-Zumar','Ghafir','Fussilat','Ash-Shura','Az-Zukhruf','Ad-Dukhan','Al-Jathiyah','Al-Ahqaf','Muhammad','Al-Fath','Al-Hujurat','Qaf','Adh-Dhariyat','At-Tur','An-Najm','Al-Qamar','Ar-Rahman',"Al-Waqi'ah",'Al-Hadid','Al-Mujadilah','Al-Hashr','Al-Mumtahanah','As-Saff','Al-Jumuah','Al-Munafiqun','At-Taghabun','At-Talaq','At-Tahrim','Al-Mulk','Al-Qalam','Al-Haqqah','Al-Maarij','Nuh','Al-Jinn','Al-Muzzammil','Al-Muddaththir','Al-Qiyamah','Al-Insan','Al-Mursalat','An-Naba','An-Naziat','Abasa','At-Takwir','Al-Infitar','Al-Mutaffifin','Al-Inshiqaq','Al-Buruj','At-Tariq','Al-Ala','Al-Ghashiyah','Al-Fajr','Al-Balad','Ash-Shams','Al-Layl','Ad-Duha','Ash-Sharh','At-Tin','Al-Alaq','Al-Qadr','Al-Bayyinah','Az-Zalzalah','Al-Adiyat',"Al-Qariah",'At-Takathur','Al-Asr','Al-Humazah','Al-Fil','Quraysh','Al-Maun','Al-Kawthar','Al-Kafirun','An-Nasr','Al-Masad','Al-Ikhlas','Al-Falaq','An-Nas'
  ];
  const digits = value => value.replace(/[٠-٩]/g, d => '0123456789'['٠١٢٣٤٥٦٧٨٩'.indexOf(d)]);
  function translate(value) {
    const text = value.trim();
    if (Object.prototype.hasOwnProperty.call(EN, text)) return EN[text];
    if (typeof SURAH_NAMES !== 'undefined') {
      const index = SURAH_NAMES.indexOf(text);
      if (index >= 0) return SURAH_EN[index] || text;
    }
    let match = text.match(/^سورة (.+?) · الآية (.+)$/);
    if (match) return `Surah ${translate(match[1])} · Verse ${digits(match[2])}`;
    match = text.match(/^صفحة\s*(.+)$/);
    if (match) return `Page ${digits(match[1])}`;
    match = text.match(/^موعد الصلاة\s*(.+)$/);
    if (match) return `Prayer time ${digits(match[1])}`;
    match = text.match(/^⌖\s*(.+)$/);
    if (match) return `⌖ ${translate(match[1])}`;
    match = text.match(/^(\d+) · (.+)$/);
    if (match) return `${digits(match[1])} · ${translate(match[2])}`;
    match = text.match(/^([\d,]+)\s*كم$/);
    if (match) return `${match[1]} km`;
    match = text.match(/^أتممت\s*(.+?) من (.+?) · (.+?)٪$/);
    if (match) return `Completed ${digits(match[1])} of ${digits(match[2])} · ${digits(match[3])}%`;
    match = text.match(/^الخرطوم، السودان · طريقة (.+)$/);
    if (match) return `Khartoum, Sudan · ${translate(match[1])} method`;
    match = text.match(/^موقعك الحالي · (.+)$/);
    if (match) return `Your location · ${translate(match[1])}`;
    match = text.match(/^المسافة إلى الكعبة المشرفة:\s*(.*)$/);
    if (match) return `Distance to the Kaaba: ${digits(match[1])}`;
    match = text.match(/^الموقع المختار:\s*(.*)$/);
    if (match) return `Selected location: ${translate(match[1])}`;
    match = text.match(/^الصفحة\s*(.+?) · (.+?) آية$/);
    if (match) return `Page ${digits(match[1])} · ${digits(match[2])} verses`;
    match = text.match(/^سورة (.+?) — (.+?) · (.+)$/);
    if (match) return `Surah ${translate(match[1])} — ${translate(match[2])} · ${digits(match[3])}`;
    return digits(text);
  }

  const originalText = new WeakMap();
  const originalAttributes = new WeakMap();
  const skipText = '#pageVerses,#adList,#tafsirMukhtasar,#tafsirSeraj,#tafsirSaadi,script,style';
  let english = store.get('lang', 'ar') === 'en';
  function translatedText(source) { return english ? translate(source) : source; }
  function localizeText(node) {
    if (!node.parentElement || !node.nodeValue.trim() || node.parentElement.closest(skipText)) return;
    const current = node.nodeValue;
    let source = originalText.get(node);
    if (source === undefined) {
      source = current;
      originalText.set(node, source);
    } else {
      const leading = source.match(/^\s*/)[0];
      const trailing = source.match(/\s*$/)[0];
      const englishVersion = leading + translate(source.trim()) + trailing;
      if (current !== source && current !== englishVersion) {
        source = current;
        originalText.set(node, source);
      }
    }
    const leading = source.match(/^\s*/)[0];
    const trailing = source.match(/\s*$/)[0];
    const next = leading + translatedText(source.trim()) + trailing;
    if (current !== next) node.nodeValue = next;
  }
  const attributeMap = {
    placeholder: {'ابحث عن سورة...':'Search for a surah…'},
    'aria-label': {
      'اختيار القارئ':'Choose reciter','ابحث عن سورة':'Search surahs','رقم الصفحة':'Page number',
      'اختيار صوت الأذان':'Choose adhan sound','مشغل التلاوة':'Recitation player',
      'الآية السابقة':'Previous verse','اتجاه القبلة بالكاميرا':'Qibla camera view',
      'تشغيل تلاوة القرآن':'Enable Quran recitation','اختر التفسير':'Choose a tafsir'
    },
    title: {'الآية السابقة':'Previous verse','الآية التالية':'Next verse'}
  };
  function localizeAttribute(el, name) {
    const current = el.getAttribute(name);
    if (current === null) return;
    let saved = originalAttributes.get(el);
    if (!saved) { saved = {}; originalAttributes.set(el, saved); }
    if (saved[name] === undefined) saved[name] = current;
    else if (current !== saved[name] && current !== (attributeMap[name]?.[saved[name]] || saved[name])) saved[name] = current;
    const next = english ? (attributeMap[name]?.[saved[name]] || saved[name]) : saved[name];
    if (current !== next) el.setAttribute(name, next);
  }
  function localizeTree(root) {
    if (root.nodeType === Node.TEXT_NODE) { localizeText(root); return; }
    if (root.nodeType !== Node.ELEMENT_NODE || root.closest?.(skipText)) return;
    ['placeholder','aria-label','title'].forEach(name => localizeAttribute(root, name));
    for (const child of root.childNodes) localizeTree(child);
  }
  function localizeQibla() {
    if (!english) return;
    localizeTree($('qibla'));
    const distance = $('qDist')?.textContent.trim().match(/^([\d,]+)\s*كم$/);
    if (distance) $('qDist').textContent = `${distance[1]} km`;
  }
  if (typeof renderQibla === 'function') {
    const renderQiblaBase = renderQibla;
    renderQibla = function (...args) {
      const result = renderQiblaBase.apply(this, args);
      localizeQibla();
      return result;
    };
  }

  const language = $('sLang');
  if (language) {
    language.options[0].value = 'ar';
    language.options[1].value = 'en';
    language.options[1].disabled = false;
    language.options[1].textContent = 'English';
  }

  const languageRow = language?.closest('.setting');
  const settingsList = document.querySelector('#settings .settings-list');
  if (languageRow && settingsList && !$('compassToggle')) {
    const row = document.createElement('div');
    row.className = 'setting';
    row.innerHTML = '<div><strong>بوصلة الهاتف</strong><small>تعمل فقط عند فتح صفحة القبلة؛ أوقفها لتقليل استخدام المستشعر والبطارية.</small></div><input class="toggle" id="compassToggle" type="checkbox" aria-label="تفعيل البوصلة">';
    settingsList.insertBefore(row, languageRow);
  }

  const compassToggle = $('compassToggle');
  if (compassToggle) {
    compassToggle.checked = store.get('compassOn', false);
    window.isCompassEnabled = () => compassToggle.checked;
    const oldRefreshCompass = window.refreshQiblaCompass;
    window.refreshQiblaCompass = function () {
      const page = $('qibla');
      if (!compassToggle.checked || !page?.classList.contains('active')) {
        if (window.NativeCompass) NativeCompass.stop();
        if (page?.classList.contains('active') && $('compassStatus')) {
          $('compassStatus').textContent = 'البوصلة متوقفة حسب الإعداد؛ تبقى زاوية القبلة محسوبة.';
        }
        return;
      }
      if (oldRefreshCompass) oldRefreshCompass();
    };
    compassToggle.addEventListener('change', () => {
      store.set('compassOn', compassToggle.checked);
      window.refreshQiblaCompass();
      if (!compassToggle.checked) {
        if (window.NativeCompass) NativeCompass.stop();
        window.resetQiblaHeading?.();
      }
    });
  }

  const audioAnchor = $('sCont')?.closest('.setting') || languageRow;
  if (settingsList && audioAnchor && !$('quranAudioToggle')) {
    const row = document.createElement('label');
    row.className = 'setting';
    row.innerHTML = '<div><strong>تشغيل تلاوة القرآن</strong><small>إظهار أدوات الصوت وتشغيل التلاوة عند الطلب</small></div><input class="toggle" id="quranAudioToggle" type="checkbox" aria-label="تشغيل تلاوة القرآن">';
    settingsList.insertBefore(row, audioAnchor);
  }

  const adhanSelect = $('sAdhanSound');
  if (adhanSelect) {
    ADHAN_FILES.azemmour = 'adhan-azemmour.mp3';
    ADHAN_FILES.makkah = 'adhan-makkah.mp3';
    ADHAN_FILES.doha = 'Adhan_Doha_Qatar_01_Fajr_Adhan.mp3';
    for (const [value, label] of [
      ['azemmour', 'أذان أزمور، المغرب · ٢:٤٣'],
      ['makkah', 'أذان مكة – تسجيل Seyfula Islam · ٣:١٧'] ,
      ['doha', 'أذان الدوحة، قطر – تسجيل مستقل لكل صلاة من الصلوات الخمس'],
    ]) {
      if (adhanSelect.querySelector(`option[value="${value}"]`)) continue;
      const option = document.createElement('option');
      option.value = value;
      option.textContent = label;
      adhanSelect.append(option);
    }
  }

  const aboutGrid = document.querySelector('#about .info-grid');
  if (aboutGrid && !document.getElementById('adhanSourceNote')) {
    const card = document.createElement('article');
    card.className = 'card info-card';
    card.id = 'adhanSourceNote';
      card.innerHTML = `<h3>مصادر تسجيلات الأذان</h3><p>أذان أزمور من المغرب (Gvido) وأذان مكة من تسجيل Seyfula Islam، وفق ترخيص CC BY 3.0.</p><p>تسجيلات الدوحة: تعرض صفحة المصدر علامة Public Domain Mark 1.0، لكن اسم الرافع مسجل Unknown. لم يتم التحقق بشكل مستقل من الملكية أو حقوق إعادة التوزيع؛ لذلك لا نؤكد أنها ملكية عامة أو مصرح بإعادة توزيعها بصورة قاطعة.</p><p><a href="https://freesound.org/people/Gvido/sounds/36997/" target="_blank" rel="noopener">Freesound · Gvido</a> · <a href="https://commons.wikimedia.org/wiki/File:Adhan,_Great_Mosque_of_Mecca_-_Jan_21,_2013.webm" target="_blank" rel="noopener">Wikimedia Commons · Seyfula Islam</a> · <a href="https://archive.org/details/adhan.recordings.from.doha.qatar" target="_blank" rel="noopener">Internet Archive · Doha recordings</a> · <a href="https://creativecommons.org/licenses/by/3.0/" target="_blank" rel="noopener">CC BY 3.0</a></p>`;
    aboutGrid.append(card);
  }

  const quranPanel = document.querySelector('.quran-panel');
  const mushafSheet = $('mushafSheet');
  const pageNavigation = document.querySelector('.page-navigation');
  const pagePrevious = $('previousPage');
  const pageNext = $('nextPage');
  if (quranPanel && mushafSheet && pageNavigation && pagePrevious && pageNext && !$('quranModeRead')) {
    const mode = document.createElement('div');
    mode.className = 'quran-mode';
    mode.setAttribute('role', 'group');
    mode.setAttribute('aria-label', 'وضع المصحف');
    mode.innerHTML = '<span class="quran-mode-label">وضع المصحف</span><button id="quranModeRead" type="button" aria-pressed="false">قراءة فقط</button><button id="quranModeListen" type="button" aria-pressed="true">قراءة مع التلاوة</button>';
    document.querySelector('.player2')?.before(mode);

    const pageJump = pageNavigation.querySelector('.page-jump');
    const goPage = $('goPage');
    pageNavigation.replaceChildren(pageJump, goPage);

    const turns = document.createElement('div');
    turns.className = 'mushaf-page-controls';
    turns.setAttribute('aria-label', 'تقليب صفحات المصحف');
    pagePrevious.innerHTML = '<span aria-hidden="true">‹</span> الصفحة السابقة';
    pageNext.innerHTML = 'الصفحة التالية <span aria-hidden="true">›</span>';
    const turnHint = document.createElement('span');
    turnHint.className = 'mushaf-turn-hint';
    turnHint.textContent = 'اسحب الصفحة أو استخدم زري التنقل بجانب المصحف.';
    turns.append(pagePrevious, turnHint, pageNext);
    mushafSheet.before(turns);

    const style = document.createElement('style');
    style.textContent = `
      .quran-mode{display:flex;align-items:center;justify-content:center;gap:7px;flex-wrap:wrap;margin:8px auto 12px;max-width:620px}
      .quran-mode-label{font-size:12px;color:var(--muted);margin-inline-end:3px}
      .quran-mode button{min-height:42px;padding:8px 13px;border:1px solid var(--line);border-radius:11px;background:var(--paper);color:var(--ink);font-size:13px}
      .quran-mode button[aria-pressed="true"]{background:#174238;color:#fffefa;border-color:#174238}
      .mushaf-page-controls{direction:rtl;display:flex;align-items:center;justify-content:space-between;gap:10px;max-width:580px;margin:12px auto 2px}
      .mushaf-page-controls button{flex:1;min-height:48px;display:flex;align-items:center;justify-content:center;gap:8px;padding:9px 12px;border:1px solid #c7b27d;border-radius:12px;background:var(--paper);color:var(--ink);font-size:14px;font-weight:600}
      .mushaf-page-controls button span{font-size:22px;color:#9b7d3e}
      .mushaf-page-controls button:disabled{opacity:.45;cursor:not-allowed}
      .mushaf-turn-hint{max-width:145px;text-align:center;color:var(--muted);font-size:11px;line-height:1.5}
      .mushaf-sheet{touch-action:pan-y}
      body.english-mode .mushaf-page-controls{direction:ltr}
      @media(max-width:650px){.quran-mode{gap:6px;margin:8px 0}.quran-mode-label{width:100%;text-align:center}.quran-mode button{flex:1;padding:8px 5px;font-size:12px}.mushaf-page-controls{gap:7px;margin:10px 0}.mushaf-page-controls button{min-height:52px;padding:8px 5px;font-size:12px}.mushaf-turn-hint{display:none}}
    `;
    document.head.append(style);

    let swipeStart = null;
    mushafSheet.addEventListener('touchstart', event => {
      const touch = event.changedTouches[0];
      swipeStart = { x: touch.clientX, y: touch.clientY };
    }, { passive: true });
    mushafSheet.addEventListener('touchend', event => {
      if (!swipeStart) return;
      const touch = event.changedTouches[0];
      const dx = touch.clientX - swipeStart.x;
      const dy = touch.clientY - swipeStart.y;
      swipeStart = null;
      if (Math.abs(dx) < 60 || Math.abs(dy) > 90) return;
      loadPage(S.page + (dx < 0 ? 1 : -1));
    }, { passive: true });
  }

  const audioToggle = $('quranAudioToggle');
  let quranAudioEnabled = store.get('quranAudioEnabled', true) !== false;
  if (audioToggle && typeof P !== 'undefined') {
    const playbackMethods = ['play', 'step', 'toggle', 'auto', 'preload'];
    for (const name of playbackMethods) {
      const original = P[name];
      P[name] = function (...args) {
        if (!quranAudioEnabled) {
          if (name === 'play') {
            const verse = this.page?.verses[args[0]];
            if (verse) {
              this.i = args[0];
              if (typeof hl === 'function') hl(`${verse.surah}:${verse.ayah}`);
              if (S.tafsirOn) window.showTafsir(verse);
            }
          }
          return;
        }
        return original.apply(this, args);
      };
    }
    audioToggle.checked = quranAudioEnabled;
    const applyAudioPreference = () => {
      document.body.classList.toggle('quran-audio-disabled', !quranAudioEnabled);
      $('quranModeRead')?.setAttribute('aria-pressed', String(!quranAudioEnabled));
      $('quranModeListen')?.setAttribute('aria-pressed', String(quranAudioEnabled));
      if (quranAudioEnabled) return;
      P.stop();
      $('floatingPlayer').hidden = true;
      try {
        if ('mediaSession' in navigator) {
          ['previoustrack', 'nexttrack', 'play', 'pause'].forEach(action =>
            navigator.mediaSession.setActionHandler(action, null));
        }
      } catch (_) {}
    };
    audioToggle.addEventListener('change', () => {
      quranAudioEnabled = audioToggle.checked;
      store.set('quranAudioEnabled', quranAudioEnabled);
      applyAudioPreference();
    });
    $('quranModeRead')?.addEventListener('click', () => {
      audioToggle.checked = false;
      audioToggle.dispatchEvent(new Event('change'));
    });
    $('quranModeListen')?.addEventListener('click', () => {
      audioToggle.checked = true;
      audioToggle.dispatchEvent(new Event('change'));
    });
    applyAudioPreference();
  }

  const tafsirTools = document.querySelector('.tafsir-tools');
  const tafsirHint = $('tafsirHint');
  const mukhtasarCard = $('tafsirMukhtasar')?.closest('.tafsir-card');
  const serajCard = $('tafsirSeraj')?.closest('.tafsir-card');
  const saadiCard = document.createElement('details');
  saadiCard.className = 'tafsir-card';
  saadiCard.id = 'tafsirSaadiCard';
  saadiCard.open = true;
  saadiCard.innerHTML = '<summary>تفسير السعدي</summary><p id="tafsirSaadi">اختر آية لعرض التفسير.</p><p class="tafsir-source" id="tafsirSaadiCredit">تفسير السعدي - تيسير الكريم الرحمن، الإصدار 1.0.0، من QuranEnc.</p>';
  if (serajCard && !document.getElementById('tafsirSaadiCard')) serajCard.before(saadiCard);

  let sourceSelect;
  let activeTafsirVerse = null;
  let saadiRequest = 0;
  const saadiCache = new Map();
  if (tafsirTools && mukhtasarCard && serajCard) {
    const sourceLabel = document.createElement('label');
    sourceLabel.className = 'tafsir-source-choice';
    sourceLabel.innerHTML = '<span>التفسير</span><select class="select" id="tafsirSourceSelect" aria-label="اختر التفسير"><option value="arabic_mokhtasar">تفسير المختصر</option><option value="arabic_saadi">تفسير السعدي</option></select>';
    tafsirTools.insertBefore(sourceLabel, tafsirHint);
    sourceSelect = $('tafsirSourceSelect');
    sourceSelect.value = store.get('tafsirSource', 'arabic_mokhtasar');
    if (!['arabic_mokhtasar', 'arabic_saadi'].includes(sourceSelect.value)) {
      sourceSelect.value = 'arabic_mokhtasar';
    }
    const applyTafsirSource = () => {
      const isSaadi = sourceSelect.value === 'arabic_saadi';
      mukhtasarCard.hidden = isSaadi;
      saadiCard.hidden = !isSaadi;
    };
    applyTafsirSource();

    const originalResetTafsir = window.resetTafsir;
    window.resetTafsir = function (...args) {
      saadiRequest++;
      activeTafsirVerse = null;
      const result = originalResetTafsir.apply(this, args);
      $('tafsirSaadi').textContent = '';
      applyTafsirSource();
      return result;
    };

    const originalShowTafsir = window.showTafsir;
    window.showTafsir = async function (verse) {
      activeTafsirVerse = verse;
      const ticket = ++saadiRequest;
      const useSaadi = sourceSelect.value === 'arabic_saadi';
      applyTafsirSource();
      const result = await originalShowTafsir.call(this, verse);
      if (!useSaadi || ticket !== saadiRequest || !verse) return result;

      const target = $('tafsirSaadi');
      target.textContent = 'جارٍ تحميل التفسير...';
      try {
        let request = saadiCache.get(verse.surah);
        if (!request) {
          request = Promise.resolve().then(() => {
            const raw = window.NativeTafsir?.loadSaadiSura?.(verse.surah);
            if (!raw) throw new Error('offline tafsir is unavailable');
            const rows = JSON.parse(raw);
            if (!Array.isArray(rows)) throw new Error('invalid offline tafsir');
            return rows;
          });
          saadiCache.set(verse.surah, request);
          request.catch(() => saadiCache.delete(verse.surah));
        }
        const rows = await request;
        const text = rows
          .filter(row => row.from_aya <= verse.ayah && row.to_aya >= verse.ayah)
          .map(row => row.text)
          .join('\n\n');
        if (ticket === saadiRequest) {
          target.textContent = text || 'لم يرد تفسير مستقل لهذه الآية في المصدر.';
          if (tafsirHint) tafsirHint.textContent = 'يعمل التفسير المحدد دون اتصال بالإنترنت.';
        }
      } catch (_) {
        if (ticket === saadiRequest) target.textContent = 'تعذّر تحميل التفسير المحلي.';
      }
      return result;
    };

    sourceSelect.addEventListener('change', () => {
      store.set('tafsirSource', sourceSelect.value);
      applyTafsirSource();
      if (activeTafsirVerse && S.tafsirOn) window.showTafsir(activeTafsirVerse);
    });
  }

  const qiblaInfo = document.querySelector('.qibla-info');
  const bearingValue = $('qDeg')?.closest('.big');
  if (qiblaInfo && bearingValue && !$('qiblaBearingLabel')) {
    const bearingLabel = document.createElement('div');
    bearingLabel.id = 'qiblaBearingLabel';
    bearingLabel.className = 'eyebrow';
    bearingLabel.textContent = 'زاوية القبلة من الشمال الحقيقي';
    bearingValue.before(bearingLabel);
    const northNote = document.createElement('small');
    northNote.className = 'compass-status';
    northNote.textContent = 'علامة الشمال تشير إلى الشمال الحقيقي؛ والسهم الذهبي يشير إلى القبلة.';
    bearingValue.after(northNote);
  }

  const reciters = [
    [ABDULKAREEM, 'محمد عبد الكريم · سوداني'],
    [ALZAIN, 'الزين محمد أحمد · سوداني'],
    ['ar.noreenmuhammadsiddiq', 'نورين محمد صديق · سوداني']
  ];
  for (const select of [$('reciterSelect'), $('dlRec')].filter(Boolean)) {
    for (const [value, label] of reciters) {
      if (![...select.options].some(option => option.value === value)) select.add(new Option(label, value));
    }
    if (select.id === 'reciterSelect') select.value = S.reciter;
  }
  AUDIO_BASE[ABDULKAREEM] = 'https://everyayah.com/data/Muhammad_AbdulKareem_128kbps/';
  const recitationCredits = document.querySelector('#about .info-card:nth-child(4) p');
  if (recitationCredits) recitationCredits.innerHTML = 'عرض النص القرآني عبر <a href="https://quran.com" target="_blank" rel="noopener">Quran.com</a>. تسجيلات مشاري العفاسي والسديس من Quran.com، وماهر المعيقلي ومحمد عبدالكريم من <a href="https://everyayah.com" target="_blank" rel="noopener">EveryAyah</a>. تسجيلات الزين محمد أحمد ونورين محمد صديق آيةً آيةً عبر <a href="https://cdn.mualim.app/" target="_blank" rel="noopener">Mualim.app</a>، ضمن <a href="https://huggingface.co/datasets/zaibihassan/Quranic-Recitation-Data" target="_blank" rel="noopener">Quranic Recitation Data</a> (ترخيص مجموعة البيانات: Apache-2.0). تبقى حقوق التسجيلات لأصحابها ومزوديها وتخضع لشروطهم.';
  const migrateLegacyAlzainDownloads = async () => {
    const oldDownloads = (await DB.all('surah')).filter(({k, v}) =>
      k.startsWith(`${ALZAIN}:`) && v?.full);
    if (!oldDownloads.length) return;
    const surahs = oldDownloads.map(({k}) => k.slice(ALZAIN.length + 1));
    for (const surah of surahs) {
      const key = `${ALZAIN}:full:${surah}`;
      const url = BU.get(key);
      if (url) { URL.revokeObjectURL(url); BU.delete(key); }
    }
    await DB.delMany('audio', surahs.map(surah => `${ALZAIN}:full:${surah}`));
    await DB.delMany('surah', oldDownloads.map(({k}) => k));
    oldDownloads.forEach(({k}) => DLSET.delete(k));
    await renderDLList();
    renderSurahList();
  };
  migrateLegacyAlzainDownloads().catch(() => {});

  let salawatAudio;
  speakSalawat = function (volume) {
    const level = Math.max(0, Math.min(1, Number(volume) || 0.6));
    if (window.NativeAlarms?.previewSalawat) {
      NativeAlarms.previewSalawat(level);
      return;
    }
    if (!salawatAudio) salawatAudio = new Audio('salawat.mp3');
    salawatAudio.volume = level;
    salawatAudio.currentTime = 0;
    salawatAudio.play().catch(() => notify('تعذّر تشغيل الصوت'));
  };
  const soundNote = [...document.querySelectorAll('#settings small')].find(el => el.textContent.includes('الصلاة على النبي تُنطق'));
  if (soundNote) soundNote.textContent = 'الأذكار مسجلة محليًا؛ والصلاة على النبي لها صوت مدمج يعمل دون إنترنت';

  const style = document.createElement('style');
  style.textContent = `
    body.english-mode{font-family:system-ui,-apple-system,"Segoe UI",sans-serif;direction:ltr}
    body.english-mode .mushaf-sheet{direction:rtl}
    .setting select,.setting input.toggle{touch-action:manipulation}
    body.quran-audio-disabled #reciterSelect,body.quran-audio-disabled .player2,body.quran-audio-disabled #floatingPlayer{display:none!important}
    .tafsir-source-choice{display:flex;align-items:center;gap:8px;font-size:12px;color:var(--muted)}
    .tafsir-source-choice .select{min-height:42px;min-width:150px}
    .memorial-footer{margin:34px 0 10px;padding:18px 20px;border:1px solid var(--line);border-radius:16px;background:var(--paper);color:var(--ink);text-align:center;line-height:1.9}
    .memorial-footer strong,.memorial-footer span{display:block}
    .memorial-footer strong{font:700 16px Amiri,serif;color:var(--green)}
    .memorial-footer span{font-size:12px;color:var(--muted)}
    .memorial-overlay{position:fixed;inset:0;z-index:220;display:grid;place-items:center;padding:20px;background:#071b16b8;overscroll-behavior:contain}
    .memorial-overlay[hidden]{display:none!important;pointer-events:none!important;visibility:hidden!important}
    .memorial-card{width:min(100%,430px);padding:28px 24px;border:1px solid #d8c28e;border-radius:22px;background:var(--paper);color:var(--ink);box-shadow:0 24px 80px #0005;text-align:center;touch-action:manipulation}
    .memorial-card .mark{color:#b4934c;font-size:28px}
    .memorial-card h2{margin:7px 0 12px;font:700 24px Amiri,serif}
    .memorial-card p{margin:0;color:var(--muted);line-height:2}
    .memorial-card button{min-height:48px;margin-top:22px;padding:0 26px;border:0;border-radius:12px;background:var(--green);color:white;font:700 15px system-ui,sans-serif;cursor:pointer;touch-action:manipulation}
    body.english-mode .memorial-card{direction:ltr}
    body.english-mode .memorial-footer strong{font:700 16px system-ui,sans-serif}
  `;
  document.head.append(style);
  const footer = document.createElement('footer');
  footer.className = 'memorial-footer';
  footer.innerHTML = '<strong>وقفٌ لروح المرحومة عزيزة القنديل علي</strong><span>نرجو الدعاء لها</span>';
  document.querySelector('.main')?.append(footer);
  const memorial = document.createElement('div');
  memorial.id = 'memorialModal';
  memorial.className = 'memorial-overlay';
  memorial.hidden = true;
  memorial.setAttribute('aria-hidden', 'true');
  memorial.innerHTML = '<section class="memorial-card" role="dialog" aria-modal="true" aria-labelledby="memorialTitle"><div class="mark" aria-hidden="true">۞</div><h2 id="memorialTitle">دعاء وصدقة جارية</h2><p>هذا التطبيق وقفٌ لروح المرحومة عزيزة القنديل علي، نرجو الدعاء لها.</p><button id="memorialClose" type="button">متابعة</button></section>';
  document.body.append(memorial);
  const closeMemorial = () => { memorial.hidden = true; memorial.setAttribute('aria-hidden', 'true'); };
  $('memorialClose').addEventListener('click', closeMemorial);
  memorial.addEventListener('click', event => { if (event.target === memorial) closeMemorial(); });
  document.addEventListener('keydown', event => { if (event.key === 'Escape' && !memorial.hidden) closeMemorial(); });

  let languageObserver;
  function formatDate(locale, fallback, options, today) {
    try { return new Intl.DateTimeFormat(locale, options).format(today); }
    catch (_) {
      try { return new Intl.DateTimeFormat(fallback, options).format(today); }
      catch (_) { return ''; }
    }
  }
  function applyLanguage() {
    languageObserver?.disconnect();
    try {
      english = language?.value === 'en' || store.get('lang', 'ar') === 'en';
      document.documentElement.lang = english ? 'en' : 'ar';
      document.documentElement.dir = english ? 'ltr' : 'rtl';
      document.body.classList.toggle('english-mode', english);
      if (language) language.value = english ? 'en' : 'ar';
      const today = new Date();
      const greg = $('gregDate');
      const hijri = $('hijriDate');
      const gregOptions = {weekday:'long',day:'numeric',month:'long',year:'numeric'};
      const hijriOptions = {day:'numeric',month:'long',year:'numeric'};
      if (greg) greg.textContent = formatDate(english ? 'en-US' : 'ar-EG-u-nu-arab', english ? 'en' : 'ar', gregOptions, today);
      if (hijri) hijri.textContent = formatDate(english ? 'en-US-u-ca-islamic-umalqura' : 'ar-SA-u-ca-islamic-umalqura', english ? 'en-u-ca-islamic' : 'ar-u-ca-islamic', hijriOptions, today);
      localizeTree(document.body);
      localizeQibla();
    } finally {
      languageObserver?.observe(document.body, {subtree:true,childList:true,characterData:true,attributes:true,attributeFilter:['placeholder','aria-label','title']});
    }
  }
  let languageApplyTimer;
  const handleLanguageChange = () => {
    store.set('lang', language.value === 'en' ? 'en' : 'ar');
    clearTimeout(languageApplyTimer);
    languageApplyTimer = setTimeout(applyLanguage, 0);
  };
  if (language) {
    language.addEventListener('change', handleLanguageChange, {passive:true});
    language.addEventListener('input', handleLanguageChange, {passive:true});
  }
  languageObserver = new MutationObserver(records => {
    for (const record of records) {
      if (record.type === 'characterData') localizeText(record.target);
      else if (record.type === 'attributes') localizeAttribute(record.target, record.attributeName);
      else record.addedNodes.forEach(localizeTree);
    }
  });
  languageObserver.observe(document.body, {subtree:true,childList:true,characterData:true,attributes:true,attributeFilter:['placeholder','aria-label','title']});
  applyLanguage();
  // Keep the memorial permanently visible in the footer, but avoid interrupting
  // the user with the same dedication dialog on every app launch.
  let memorialAlreadyShown = false;
  try { memorialAlreadyShown = localStorage.getItem('noorAlImanMemorialDialogSeen') === '1'; } catch (_) {}
  if (!memorialAlreadyShown) {
    setTimeout(() => {
      memorial.hidden = false;
      memorial.setAttribute('aria-hidden', 'false');
      $('memorialClose')?.focus();
      try { localStorage.setItem('noorAlImanMemorialDialogSeen', '1'); } catch (_) {}
    }, 500);
  }

  const prayerAdjustmentRows = [
    ['fajr', 'الفجر'], ['sunrise', 'الشروق'], ['dhuhr', 'الظهر'],
    ['asr', 'العصر'], ['maghrib', 'المغرب'], ['isha', 'العشاء']
  ];
  const prayerAdjustments = Object.fromEntries(prayerAdjustmentRows.map(([key]) => {
    const raw = Number(store.get('prayerAdjustments', {})?.[key] || 0);
    return [key, Number.isFinite(raw) ? Math.max(-180, Math.min(180, Math.trunc(raw))) : 0];
  }));
  const originalDayTimes = dayTimes;
  dayTimes = function (city, method, date, asr) {
    return originalDayTimes(city, method, date, asr).map(prayer => {
      const original = prayer.t;
      const offset = prayerAdjustments[prayer.k] || 0;
      return {...prayer, originalTime: original,
        t: original ? new Date(original.getTime() + offset * 60_000) : null};
    });
  };

  const prayerEnhancements = document.createElement('section');
  prayerEnhancements.className = 'prayer-enhancements';
  prayerEnhancements.innerHTML = `
    <div class="setting setting-head"><div><strong>إعدادات الأذان ومواقيت الصلاة</strong><small>تحكم مستقل في التنبيه والتوقيت</small></div></div>
    <div class="setting"><div><strong>إظهار أدوات التحكم في الإشعار</strong><small>هذا الخيار لا يوقف صوت الأذان. قد يعرض Android إشعارًا عامًا إلزاميًا أثناء تشغيل الخدمة.</small></div><input class="toggle" id="adhanNotificationToggle" type="checkbox" aria-label="إظهار أدوات التحكم في الإشعار"></div>
    <div class="setting"><div><strong>إيقاف الأذان عند قلب الهاتف</strong><small>اقلب الهاتف على وجهه أثناء تشغيل الأذان لإيقاف الصوت تلقائيًا.</small></div><input class="toggle" id="stopAdhanFaceDownToggle" type="checkbox" aria-label="إيقاف الأذان عند قلب الهاتف"></div>
    <div class="setting"><div><strong>إيقاف الأذان بأزرار الصوت</strong><small>يعمل هذا الخيار عندما تكون شاشة الأذان ظاهرة؛ وقد لا يستجيب إذا منع Android ظهورها.</small></div><input class="toggle" id="stopAdhanVolumeKeysToggle" type="checkbox" aria-label="إيقاف الأذان بأزرار الصوت"></div>
    <div class="setting"><div><strong>إيقاف الأذان عند إطفاء الشاشة</strong><small>يوقف الأذان عند ضغط زر التشغيل أو عند انطفاء الشاشة تلقائيًا أثناء التشغيل.</small></div><input class="toggle" id="stopAdhanScreenOffToggle" type="checkbox" aria-label="إيقاف الأذان عند إطفاء الشاشة"></div>
    <div class="setting"><div><strong>دقة مواعيد الأذان والتنبيه</strong><small id="exactAlarmAccessStatus">جارٍ التحقق من إذن التنبيهات الدقيقة...</small></div><button class="sound-preview" id="exactAlarmAccessButton" type="button">تفعيل الإذن</button></div>
    <div class="setting"><div><strong>تشخيص آخر تنبيه</strong><small id="alarmDiagnosticsStatus">لا توجد بيانات تشخيص بعد.</small></div><button class="sound-preview" id="refreshAlarmDiagnosticsButton" type="button">تحديث</button></div>
    <div class="setting"><div><strong>شاشة الأذان عند قفل الهاتف</strong><small id="fullScreenAccessStatus">يسمح Android بعرض شاشة الأذان عند توفر صلاحية ملء الشاشة.</small></div><button class="sound-preview" id="fullScreenAccessButton" type="button">ضبط الإذن</button></div>
    <div class="adjustment-heading"><strong>الضبط اليدوي لمواقيت الصلاة</strong><small>التعديل بالدقائق فوق الحساب الأصلي، ويحفظ تلقائيًا.</small></div>
    <div class="prayer-adjustment-list" id="prayerAdjustmentList" aria-live="polite"></div>
    <div class="adjustment-actions"><button class="adjustment-reset-all" id="resetAllPrayerAdjustments" type="button">إعادة ضبط جميع التعديلات</button></div>
    <p class="audio-rights-note">لا تتوفر في الملفات الحالية تسجيلات من الحرمين بترخيص إعادة توزيع موثق.</p>`;
  settingsList?.append(prayerEnhancements);

  const prayerEnhancementStyle = document.createElement('style');
  prayerEnhancementStyle.textContent = `
    .prayer-enhancements{grid-column:1/-1;border-top:1px solid var(--line);margin-top:8px;padding-top:8px}
    .adjustment-heading{display:grid;gap:3px;padding:14px 19px 8px;color:var(--ink)}
    .adjustment-heading small,.prayer-adjustment small{font-size:11px;color:var(--muted);line-height:1.6}
    .prayer-adjustment-list{display:grid;gap:8px;padding:8px 14px}
    .prayer-adjustment{display:grid;grid-template-columns:minmax(110px,1fr) minmax(130px,1.2fr) auto;align-items:center;gap:10px;padding:11px;border:1px solid var(--line);border-radius:12px;background:var(--paper)}
    .prayer-adjustment-name{font-weight:700;color:var(--ink)}
    .prayer-adjustment-times{display:grid;gap:2px;font-size:11px;color:var(--muted)}
    .prayer-adjustment-times b{color:var(--ink);font-size:12px}
    .prayer-adjustment-controls{display:flex;align-items:center;gap:5px;direction:ltr}
    .prayer-adjustment-controls button,.adjustment-reset-all{min-width:38px;min-height:38px;border:1px solid var(--line);border-radius:10px;background:var(--soft);color:var(--green);font-weight:700}
    .prayer-adjustment-controls .adjustment-value{min-width:46px;text-align:center;font-size:12px;color:var(--ink);direction:rtl}
    .prayer-adjustment-controls .adjustment-reset{min-width:42px;font-size:10px;font-weight:500;background:transparent}
    .adjustment-actions{display:flex;justify-content:flex-start;padding:4px 14px 12px}
    .adjustment-reset-all{padding:0 13px;font-size:12px}
    .audio-rights-note{margin:0 14px 12px;padding:10px 12px;border-radius:10px;background:var(--soft);color:var(--muted);font-size:11px;line-height:1.6}
    .adhan-stop-bar{position:fixed;z-index:150;left:50%;bottom:calc(82px + env(safe-area-inset-bottom));transform:translateX(-50%);width:min(92vw,440px);padding:10px;border:1px solid #b77575;border-radius:14px;background:#fff7f5;box-shadow:0 8px 30px #102c2630;direction:rtl}
    .adhan-stop-bar[hidden]{display:none}
    .adhan-stop-bar button{width:100%;min-height:48px;border:0;border-radius:10px;background:#a53333;color:#fff;font-weight:700;font-size:15px}
    .dark .prayer-adjustment{background:#20332c}.dark .prayer-adjustment-controls button{background:#263b34;color:#e7e9df}.dark .adhan-stop-bar{background:#302626}
    @media(min-width:651px){.adhan-stop-bar{bottom:20px}}
    @media(max-width:650px){.prayer-adjustment{grid-template-columns:1fr auto;gap:7px}.prayer-adjustment-name{grid-column:1}.prayer-adjustment-times{grid-column:1;grid-row:2}.prayer-adjustment-controls{grid-column:2;grid-row:1/3}.prayer-adjustment-controls button{min-width:35px}.prayer-adjustment-controls .adjustment-reset{min-width:37px}}
  `;
  
document.head.append(prayerEnhancementStyle);

  // Present settings as a dedicated page with six expandable categories.
  if (settingsList && !settingsList.dataset.organized) {
    settingsList.dataset.organized = 'true';
    settingsList.classList.add('settings-hub');
    const groups = [
      {title:'الموقع ومواقيت الصلاة', icon:'⌖', ids:['settingCity','sMethod','sAsr']},
      {title:'الأذان والتنبيهات', icon:'◖', ids:['sNotify','sReminder','sAdhanSound','adhanNotificationToggle','stopAdhanFaceDownToggle','stopAdhanVolumeKeysToggle','stopAdhanScreenOffToggle','fullScreenAccessButton']},
      {title:'الأذكار والتسبيح', icon:'☀', ids:['tsOn','tsEvery','tsType','tsFrom','tsTo','tsVol']},
      {title:'المصحف والقبلة', icon:'۞', ids:['sCont','quranAudioToggle','compassToggle']},
      {title:'المظهر واللغة', icon:'◐', ids:['darkToggle','sLang']}
    ];
    const rowFor = id => [...settingsList.children].find(node =>
      node.classList?.contains('setting') && node.querySelector('#' + id));
    let accordionIndex = 0;
    const makeAccordion = (title, icon, wide = false) => {
      const details = document.createElement('details');
      const stateKey = 'settingsAccordion' + (++accordionIndex);
      details.open = store.get(stateKey, false) === true;
      details.addEventListener('toggle', () => store.set(stateKey, details.open));
      details.className = 'settings-group settings-accordion' + (wide ? ' settings-accordion--wide' : '');
      const summary = document.createElement('summary');
      summary.className = 'settings-group-title settings-accordion-summary';
      const mark = document.createElement('span');
      mark.className = 'settings-group-icon';
      mark.setAttribute('aria-hidden', 'true');
      mark.textContent = icon;
      const label = document.createElement('span');
      label.textContent = title;
      summary.append(mark, label);
      const panel = document.createElement('div');
      panel.className = 'settings-accordion-panel';
      details.append(summary, panel);
      return {details, panel};
    };
    for (const group of groups) {
      const {details, panel} = makeAccordion(group.title, group.icon);
      for (const id of group.ids) {
        const row = rowFor(id);
        if (row) panel.append(row);
      }
      if (panel.querySelector('.setting')) settingsList.append(details);
    }
    const {details: adjustmentCard, panel: adjustmentPanel} = makeAccordion('تخصيص مواقيت الصلاة', '◷', true);
    if (prayerEnhancements) adjustmentPanel.append(prayerEnhancements);
    if (adjustmentPanel.children.length) settingsList.append(adjustmentCard);
    for (const node of [...settingsList.children]) {
      if (node.classList?.contains('setting')) {
        const {details, panel} = makeAccordion('إعدادات إضافية', '⚙');
        panel.append(node);
        settingsList.append(details);
      }
    }
  }

const settingsHubStyle = document.createElement('style');
settingsHubStyle.textContent = `
  .hero{background:radial-gradient(circle at 14% 18%,#d2b16a2b 0 2%,transparent 28%),radial-gradient(circle at 88% 90%,#9bc1a51a 0 3%,transparent 34%),linear-gradient(115deg,#143c32,#1f5848)}
  .card{background:linear-gradient(145deg,var(--paper),#ffffff00 72%),var(--paper);border-color:color-mix(in srgb,var(--line) 82%,var(--gold) 18%)}
  .prayer{position:relative;overflow:hidden;transition:transform .2s,border-color .2s,box-shadow .2s}
  .prayer:after{content:'۞';position:absolute;left:-4px;bottom:-13px;color:#caa76616;font:38px Amiri,serif;pointer-events:none}
  .prayer.next{background:linear-gradient(145deg,#f8f0dd,#e8f0e9);border-color:#caa766;box-shadow:0 7px 18px #b4934a18}
  .prayer:hover{transform:translateY(-2px);border-color:#caa76688}
  .mini-icon{background:linear-gradient(145deg,#f8f0dc,#e7eee8);box-shadow:inset 0 0 0 1px #caa76622}
  .daily-verse{background:linear-gradient(135deg,var(--paper),#f4efdf66)}
  .tasbeeh{background:linear-gradient(145deg,var(--paper),#edf3ee)}
  .bead{background:radial-gradient(circle at 30% 24%,#6b9b7c,#245543 52%,#102c26 100%);border:1px solid #caa76688}
  .dark .card{background:linear-gradient(145deg,#23372f,#1c2e28 78%);border-color:#405448}
  .dark .prayer.next{background:linear-gradient(145deg,#3b3a2d,#263c32);border-color:#b99a5c;box-shadow:0 7px 18px #0003}
  .dark .daily-verse{background:linear-gradient(135deg,#24382f,#1d3029)}
  .dark .tasbeeh{background:linear-gradient(145deg,#263c32,#1b3029)}

  .settings-shortcut{display:inline-flex;align-items:center;justify-content:center;gap:7px;min-height:40px;padding:0 12px;border:1px solid var(--line);border-radius:12px;background:var(--paper);color:var(--green);font:600 12px "DM Sans",Amiri,sans-serif;box-shadow:0 3px 12px #17352e0a;transition:background .2s,border-color .2s}
  .settings-shortcut span{display:grid;place-items:center;width:24px;height:24px;border-radius:8px;background:#f3eee1;color:#8b7040;font-size:17px}
  .settings-shortcut:hover,.settings-shortcut.active{border-color:#caa766;background:#f8f4e9}
  .settings-shortcut:focus-visible{outline:3px solid #caa76688;outline-offset:2px}
  @media(max-width:650px){.settings-shortcut{width:40px;min-width:40px;height:40px;padding:0;border-radius:12px}.settings-shortcut small{display:none}}
  .settings-list.settings-hub{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px;padding:0;background:transparent;border:0;box-shadow:none}

  .dark .counter{color:#f1d58f;text-shadow:0 2px 18px #e6c87924}
  .dark .tasbeeh{background:#1b3029;border-color:#40584b}
  .dark .counter-label{color:#f0e8d5}
  .settings-page-heading{align-items:center;gap:14px}
  .settings-close{display:inline-flex;align-items:center;gap:10px;min-height:42px;padding:0 15px;border:1px solid var(--line);border-radius:13px;background:var(--paper);color:var(--ink);font-size:13px;font-weight:700}
  .settings-close span{display:grid;place-items:center;width:23px;height:23px;border-radius:8px;background:#f3eee1;color:#8b7040;font-size:20px;line-height:1}
  .dark .settings-close{background:#20332c;border-color:#40584b;color:#f0eadc}
  .dark .settings-close span{background:#34473d;color:#e6c77f}
  .settings-list.settings-hub{gap:13px}
  .settings-accordion{overflow:hidden;padding:0;border:1px solid var(--line);border-radius:18px;background:var(--paper);box-shadow:var(--shadow);transition:border-color .2s,box-shadow .2s}
  .settings-accordion[open]{border-color:#caa76699;box-shadow:0 12px 32px #17352e12}
  .settings-accordion-summary{list-style:none;display:flex;align-items:center;gap:12px;min-height:76px;padding:13px 16px;cursor:pointer;color:var(--ink);font:700 17px Amiri,serif}
  .settings-accordion-summary::-webkit-details-marker{display:none}
  .settings-accordion-summary:after{content:'⌄';margin-inline-start:auto;color:#9b8352;font:22px system-ui;transition:transform .2s}
  .settings-accordion[open]>.settings-accordion-summary:after{transform:rotate(180deg)}
  .settings-accordion-summary .settings-group-icon{width:42px;height:42px;border-radius:14px;background:linear-gradient(145deg,#f7f0df,#e8eee7);color:#806b40;font-size:21px}
  .settings-accordion-panel{padding:0 16px 12px;border-top:1px solid var(--line)}
  .settings-accordion--wide{grid-column:1/-1}
  .dark .settings-accordion{background:#1d3029;border-color:#405448}
  .dark .settings-accordion-summary{color:#f0eadc}
  .dark .settings-accordion-summary .settings-group-icon{background:linear-gradient(145deg,#34473b,#283b33);color:#e5c77e}
  .dark .settings-accordion-panel{border-color:#405448}
  @media(max-width:760px){.settings-accordion--wide{grid-column:auto}}
  @media(max-width:650px){.settings-page-heading{align-items:flex-start}.settings-close{min-height:38px;padding:0 10px;font-size:11px}.settings-accordion-summary{min-height:68px;padding:11px 13px;font-size:16px}.settings-accordion-panel{padding:0 13px 10px}}

  .settings-group{min-width:0}
  .settings-group--wide{grid-column:1/-1}
  .settings-group-title{display:flex;align-items:center;gap:10px;color:var(--ink);font:700 17px Amiri,serif}
  .settings-group-icon{display:grid;place-items:center;flex:none;width:34px;height:34px;border-radius:11px;background:#f3eee1;color:#8b7040;font:20px system-ui,sans-serif}
  .dark .settings-group-icon{background:#26372f;color:#e0c37e}
  .settings-group .setting{gap:12px;padding:13px 0}
  .settings-group .setting>div:first-child{min-width:0}
  .settings-group .setting strong{font-size:15px}
  .settings-group .setting small{display:block;max-width:480px;line-height:1.6}
  .settings-group .setting .select{max-width:52%;min-width:110px}
  .settings-group .prayer-enhancements{border:0;margin:0;padding:0}
  .settings-group .prayer-enhancements>.setting-head{margin:0 -17px;padding:12px 17px}
  .settings-group .adjustment-heading{padding-inline:0}
  .settings-group .prayer-adjustment-list{padding-inline:0}
  .settings-group .adjustment-actions{padding-inline:0}
  .settings-group .audio-rights-note{margin-inline:0}
  @media(max-width:760px){.settings-list.settings-hub{grid-template-columns:1fr;gap:11px}.settings-group--wide{grid-column:auto}}
  @media(max-width:650px){.settings-group{padding:12px 14px;border-radius:15px}.settings-group .setting{padding:12px 0}.settings-group .setting strong{font-size:14px}.settings-group .setting small{font-size:10px}.settings-group .setting .select{min-width:100px;max-width:55%}}
`;
document.head.append(settingsHubStyle);


  const notificationToggle = $('adhanNotificationToggle');
  const faceDownToggle = $('stopAdhanFaceDownToggle');
  const volumeKeysToggle = $('stopAdhanVolumeKeysToggle');
  const screenOffToggle = $('stopAdhanScreenOffToggle');
  const fullScreenAccessButton = $('fullScreenAccessButton');
  const fullScreenAccessStatus = $('fullScreenAccessStatus');
  const exactAlarmAccessButton = $('exactAlarmAccessButton');
  const exactAlarmAccessStatus = $('exactAlarmAccessStatus');
  const alarmDiagnosticsStatus = $('alarmDiagnosticsStatus');
  const refreshAlarmDiagnosticsButton = $('refreshAlarmDiagnosticsButton');
  const notificationAccessStatus = document.createElement('small');
  const notificationAccessButton = document.createElement('button');
  notificationAccessButton.type = 'button';
  notificationAccessButton.textContent = 'إعدادات الإشعارات';
  notificationAccessButton.className = exactAlarmAccessButton?.className || '';
  notificationAccessButton.addEventListener('click', () =>
    window.NativeAlarms?.openNotificationSettings?.());
  if (exactAlarmAccessButton?.parentElement) {
    const notificationAccessRow = document.createElement('div');
    notificationAccessRow.className = 'notification-access-row';
    notificationAccessRow.append(notificationAccessStatus, notificationAccessButton);
    exactAlarmAccessButton.parentElement.insertAdjacentElement('afterend', notificationAccessRow);
  }
  const showAdhanNotification = store.get('showAdhanNotification', true) !== false;
  const stopAdhanFaceDown = store.get('stopAdhanFaceDown', false) === true;
  const stopAdhanVolumeKeys = store.get('stopAdhanVolumeKeys', false) === true;
  const stopAdhanScreenOff = store.get('stopAdhanScreenOff', false) === true;
  if (notificationToggle) notificationToggle.checked = showAdhanNotification;
  if (faceDownToggle) faceDownToggle.checked = stopAdhanFaceDown;
  if (volumeKeysToggle) volumeKeysToggle.checked = stopAdhanVolumeKeys;
  if (screenOffToggle) screenOffToggle.checked = stopAdhanScreenOff;
  const adhanEnableRow = $('sNotify')?.closest('.setting');
  if (adhanEnableRow) {
    const title = adhanEnableRow.querySelector('strong');
    const note = adhanEnableRow.querySelector('small');
    if (title) title.textContent = 'تشغيل الأذان في أوقاته';
    if (note) note.textContent = 'تشغيل صوت الأذان عند دخول وقت الصلاة، حتى عند إغلاق التطبيق';
  }
  function syncFullScreenAccessStatus() {
    const allowed = !window.NativeAlarms?.canUseFullScreenIntent
      || NativeAlarms.canUseFullScreenIntent();
    if (fullScreenAccessStatus) fullScreenAccessStatus.textContent = allowed
      ? 'إذن شاشة الأذان متاح على هذا الجهاز.'
      : 'فعّل إذن ملء الشاشة من إعدادات Android؛ سيبقى الإشعار العادي متاحًا إذا رفض النظام الإذن.';
    if (fullScreenAccessButton) fullScreenAccessButton.textContent = allowed ? 'تم التفعيل' : 'ضبط الإذن';
    if (fullScreenAccessButton) fullScreenAccessButton.disabled = allowed;
  }
  function syncExactAlarmAccessStatus() {
    const allowed = !window.NativeAlarms?.canScheduleExactAlarms
      || NativeAlarms.canScheduleExactAlarms();
    if (exactAlarmAccessStatus) exactAlarmAccessStatus.textContent = allowed
      ? 'الإذن مفعّل؛ يمكن جدولة الأذان والتذكير في أوقاتهما بدقة.'
      : 'الإذن غير مفعّل؛ لن تُجدول تنبيهات الصلاة تلقائيًا حتى تفعّله. التنبيه التقريبي لا يضمن وقت الصلاة وقد لا يسمح Android بتشغيل الأذان من الخلفية.';
    if (exactAlarmAccessButton) exactAlarmAccessButton.textContent = allowed ? 'تم التفعيل' : 'تفعيل الإذن';
    if (exactAlarmAccessButton) exactAlarmAccessButton.disabled = allowed;
  }
  function syncNotificationAccessStatus() {
    const allowed = !window.NativeAlarms?.canPostNotifications
      || NativeAlarms.canPostNotifications();
    notificationAccessStatus.textContent = allowed
      ? 'إشعارات التطبيق مسموحة.'
      : 'إذن الإشعارات مرفوض؛ افتح إعدادات التطبيق وفعّله لإظهار التذكيرات وعناصر التحكم.';
    notificationAccessButton.disabled = allowed;
    notificationAccessButton.textContent = allowed ? 'الإشعارات مفعّلة' : 'فتح إعدادات الإشعارات';
  }
  function refreshAlarmDiagnostics() {
    if (!window.NativeAlarms?.getAlarmDiagnostics) return;
    try {
      const d = JSON.parse(window.NativeAlarms.getAlarmDiagnostics() || '{}');
      const tz = cityObj()?.tz || 'Africa/Khartoum';
      const stamp = value => value > 0
        ? new Intl.DateTimeFormat('ar', {timeZone: tz, dateStyle: 'short', timeStyle: 'medium'}).format(new Date(value))
        : '—';
      const delay = Number.isFinite(Number(d.lastAlarmDelayMs))
        ? Math.round(Number(d.lastAlarmDelayMs) / 1000) : 0;
      const precision = d.lastAlarmWasExact === false ? 'تقريبي (قد يتأخر)' : 'دقيق';
      const alarm = d.lastAlarmReceivedAt
        ? (d.lastAlarmType || 'تنبيه') + ' / ' + (d.lastAlarmKey || '') +
          '؛ النوع ' + precision +
          '؛ المتوقع ' + stamp(d.lastAlarmExpectedAt) +
          '؛ وصل ' + stamp(d.lastAlarmReceivedAt) + '؛ التأخير ' + delay + ' ثانية'
        : 'لم يُسجّل وصول تنبيه بعد';
      const playback = d.lastAdhanPlaybackState
        ? d.lastAdhanPlaybackState + (d.lastAdhanPlaybackDetail ? ' — ' + String(d.lastAdhanPlaybackDetail).slice(0, 90) : '')
        : 'لم تُسجّل محاولة تشغيل أذان بعد';
      const scheduleState = ({
        exact_permission_required: 'لم تُجدول تنبيهات الصلاة: يلزم إذن التنبيه الدقيق',
        scheduled_exact: 'تنبيهات الصلاة مجدولة بدقة',
        partial_schedule_failure: 'بعض تنبيهات الصلاة لم تُجدول؛ راجع سجل النظام',
        schedule_failed: 'فشلت جدولة تنبيهات الصلاة؛ راجع سجل النظام',
        no_future_alarms: 'لا توجد تنبيهات مستقبلية ضمن نطاق الجدولة',
        alarm_manager_unavailable: 'خدمة AlarmManager غير متاحة'
      })[d.prayerAlarmScheduleStatus] || 'حالة الجدولة غير معروفة';
      if (alarmDiagnosticsStatus) alarmDiagnosticsStatus.textContent =
        scheduleState + '؛ إذن التنبيه الدقيق: ' + (d.exactAlarmPermission ? 'مفعّل' : 'غير مفعّل') +
        '؛ الإشعارات: ' + (d.notificationPermission ? 'مسموحة' : 'غير مسموحة') +
        '؛ آخر وصول: ' + alarm + '؛ تشغيل الأذان: ' + playback;
    } catch (error) {
      if (alarmDiagnosticsStatus) alarmDiagnosticsStatus.textContent = 'تعذّر قراءة سجل التنبيهات.';
    }
  }
  window.refreshAlarmPermissionStatus = () => {
    syncExactAlarmAccessStatus();
    syncNotificationAccessStatus();
    syncFullScreenAccessStatus();
    refreshAlarmDiagnostics();
  };
  exactAlarmAccessButton?.addEventListener('click', () => {
    if (!window.NativeAlarms?.canScheduleExactAlarms?.()) {
      window.NativeAlarms?.requestExactAlarmAccess?.();
    }
  });
  refreshAlarmDiagnosticsButton?.addEventListener('click', refreshAlarmDiagnostics);
  function syncNativePlaybackOptions() {
    if (window.NativeAlarms?.configurePlaybackOptions) {
      NativeAlarms.configurePlaybackOptions(notificationToggle?.checked !== false);
    }
    if (window.NativeAlarms?.configureFaceDownStop) {
      NativeAlarms.configureFaceDownStop(faceDownToggle?.checked === true);
    }
    if (window.NativeAlarms?.configureVolumeKeyStop) {
      NativeAlarms.configureVolumeKeyStop(volumeKeysToggle?.checked === true);
    }
    if (window.NativeAlarms?.configureScreenOffStop) {
      NativeAlarms.configureScreenOffStop(screenOffToggle?.checked === true);
    }
  }
  function persistPrayerAdjustments() {
    store.set('prayerAdjustments', prayerAdjustments);
    if (window.NativeAlarms?.configurePrayerAdjustments) {
      NativeAlarms.configurePrayerAdjustments(JSON.stringify(prayerAdjustments));
    }
    renderAll();
    renderPrayerAdjustmentList();
  }
  function renderPrayerAdjustmentList() {
    const list = $('prayerAdjustmentList');
    if (!list) return;
    const city = cityObj(), method = methodKey(city);
    const originals = originalDayTimes(city, method, Date.now(), S.asr);
    list.replaceChildren();
    for (const prayer of originals) {
      const offset = prayerAdjustments[prayer.k] || 0;
      const adjusted = prayer.t ? new Date(prayer.t.getTime() + offset * 60_000) : null;
      const row = document.createElement('div');
      row.className = 'prayer-adjustment';
      const name = document.createElement('div');
      name.className = 'prayer-adjustment-name';
      name.textContent = prayer.n;
      const times = document.createElement('div');
      times.className = 'prayer-adjustment-times';
      times.innerHTML = `<span>الأصل: <b>${fmt(prayer.t, city.tz)}</b></span><span>بعد التعديل: <b>${fmt(adjusted, city.tz)}</b></span>`;
      const controls = document.createElement('div');
      controls.className = 'prayer-adjustment-controls';
      const button = (label, title, change, className = '') => {
        const el = document.createElement('button');
        el.type = 'button'; el.textContent = label; el.title = title;
        el.setAttribute('aria-label', `${title} ${prayer.n}`);
        if (className) el.className = className;
        el.addEventListener('click', change);
        return el;
      };
      controls.append(button('-', `إنقاص دقيقة من توقيت`, () => {
        prayerAdjustments[prayer.k] = Math.max(-180, offset - 1); persistPrayerAdjustments();
      }));
      const value = document.createElement('span');
      value.className = 'adjustment-value';
      value.textContent = `${offset > 0 ? '+' : ''}${arNum(offset)} دقيقة`;
      controls.append(value, button('+', `زيادة دقيقة على توقيت`, () => {
        prayerAdjustments[prayer.k] = Math.min(180, offset + 1); persistPrayerAdjustments();
      }));
      controls.append(button('إعادة ضبط', 'إعادة ضبط توقيت', () => {
        prayerAdjustments[prayer.k] = 0; persistPrayerAdjustments();
      }, 'adjustment-reset'));
      row.append(name, times, controls);
      list.append(row);
    }
  }
  notificationToggle?.addEventListener('change', () => {
    store.set('showAdhanNotification', notificationToggle.checked);
    syncNativePlaybackOptions();
  });
  faceDownToggle?.addEventListener('change', () => {
    store.set('stopAdhanFaceDown', faceDownToggle.checked);
    syncNativePlaybackOptions();
  });
  volumeKeysToggle?.addEventListener('change', () => {
    store.set('stopAdhanVolumeKeys', volumeKeysToggle.checked);
    syncNativePlaybackOptions();
  });
  screenOffToggle?.addEventListener('change', () => {
    store.set('stopAdhanScreenOff', screenOffToggle.checked);
    syncNativePlaybackOptions();
  });
  fullScreenAccessButton?.addEventListener('click', () => {
    NativeAlarms?.requestFullScreenIntentAccess?.();
    setTimeout(syncFullScreenAccessStatus, 900);
  });
  $('resetAllPrayerAdjustments')?.addEventListener('click', () => {
    for (const [key] of prayerAdjustmentRows) prayerAdjustments[key] = 0;
    persistPrayerAdjustments();
  });
  renderPrayerAdjustmentList();
  if (window.NativeAlarms?.configurePrayerAdjustments) {
    NativeAlarms.configurePrayerAdjustments(JSON.stringify(prayerAdjustments));
  }
  syncNativePlaybackOptions();
  syncFullScreenAccessStatus();
  syncExactAlarmAccessStatus();
  syncNotificationAccessStatus();
  refreshAlarmDiagnostics();
  document.addEventListener('visibilitychange', window.refreshAlarmPermissionStatus);
  window.addEventListener('focus', window.refreshAlarmPermissionStatus);

  const adhanStopBar = document.createElement('div');
  adhanStopBar.className = 'adhan-stop-bar';
  adhanStopBar.hidden = true;
  adhanStopBar.innerHTML = '<button type="button">إيقاف الأذان الآن</button>';
  adhanStopBar.querySelector('button').addEventListener('click', () => {
    NativeAlarms?.stopAdhan?.();
    adhanStopBar.hidden = true;
  });
  document.body.append(adhanStopBar);
  const refreshAdhanStopBar = () => {
    adhanStopBar.hidden = !(window.NativeAlarms?.isAdhanPlaying?.());
  };
  let adhanBarTimer = 0;
  const stopAdhanBarPolling = () => { if (adhanBarTimer) clearTimeout(adhanBarTimer); adhanBarTimer = 0; };
  const pollAdhanBar = () => {
    stopAdhanBarPolling();
    if (document.visibilityState !== 'visible') return;
    refreshAdhanStopBar();
    adhanBarTimer = setTimeout(pollAdhanBar, 700);
  };
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') pollAdhanBar();
    else stopAdhanBarPolling();
  });
  pollAdhanBar();
})();


// Record runtime failures for diagnostics; never clear cache, storage, or user preferences.
(() => {
  const report = (kind, message) => {
    try { window.NativeAlarms?.reportWebError?.(String(kind).slice(0,40), String(message).slice(0,700)); }
    catch (_) { /* Diagnostics must never interrupt the app. */ }
  };
  window.addEventListener('error', event => report('javascript-error', event.message || 'unknown error'));
  window.addEventListener('unhandledrejection', event => report('unhandled-rejection', event.reason?.message || event.reason || 'unknown rejection'));
})();


// v1.17 visual system: progressive, CSS-only refinement of the existing screens.
// Keeps the current DOM, feature logic, audio assets, and persisted settings intact.
(() => {
  if (document.getElementById('noor-v117-visual-system')) return;
  const style = document.createElement('style');
  style.id = 'noor-v117-visual-system';
  style.textContent = `
    :root {
      --noor-forest: #123c32;
      --noor-forest-deep: #0b2d26;
      --noor-gold: #b99555;
      --noor-gold-soft: #f2e8d3;
      --noor-paper: #fbfaf6;
      --noor-surface: #ffffff;
      --noor-ink: #20332c;
      --noor-muted: #718078;
      --noor-line: #e8e9e1;
      --noor-radius-lg: 22px;
      --noor-radius-md: 16px;
      --noor-shadow: 0 12px 34px rgba(22, 48, 38, .075);
      color-scheme: light;
    }
    html { scroll-behavior: smooth; }
    body {
      background: var(--noor-paper);
      color: var(--noor-ink);
      -webkit-font-smoothing: antialiased;
      text-rendering: optimizeLegibility;
    }
    button, input, select, textarea { font: inherit; }
    button, [role="button"], a { -webkit-tap-highlight-color: transparent; }
    button {
      transition: transform .18s ease, box-shadow .18s ease, background-color .18s ease, border-color .18s ease;
    }
    button:active { transform: translateY(1px) scale(.985); }
    :focus-visible {
      outline: 3px solid rgba(185,149,85,.72) !important;
      outline-offset: 3px !important;
    }
    .main { width: min(100% - 28px, 1080px); margin-inline: auto; }
    .main > section, .main > .page, .main .page-section { min-width: 0; }
    .card, .info-card, .quran-panel, .qibla-info, .settings-group {
      border: 1px solid var(--noor-line);
      border-radius: var(--noor-radius-lg);
      box-shadow: var(--noor-shadow);
    }
    .info-card, .settings-group { background: var(--noor-surface); }
    .settings-page-heading { gap: 14px; margin-bottom: 18px; }
    .settings-list.settings-hub { align-items: start; gap: 14px; }
    .settings-group {
      padding: 17px 19px;
      overflow: clip;
      transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
    }
    .settings-group:focus-within { border-color: rgba(185,149,85,.65); }
    .settings-group-title { letter-spacing: -.15px; }
    .settings-group-icon {
      width: 40px; height: 40px; border-radius: 13px;
      background: linear-gradient(145deg, #f7f1e5, #eee5d2);
      color: #806638;
    }
    .settings-group .setting { border-bottom: 1px solid rgba(115,130,120,.12); }
    .settings-group .setting:last-child { border-bottom: 0; }
    .settings-group .setting strong { line-height: 1.55; }
    .settings-group .setting small { color: var(--noor-muted); }
    .settings-group .select, .settings-group select {
      border: 1px solid #dfe4dc;
      border-radius: 12px;
      min-height: 42px;
      background-color: #fff;
    }
    .settings-close, .settings-group button, .adjustment-actions button {
      border-radius: 12px;
    }
    .prayer-adjustment {
      border: 1px solid var(--noor-line);
      border-radius: 15px;
      padding: 12px;
      background: #fffefa;
    }
    .prayer-adjustment-controls button {
      min-width: 38px; min-height: 38px;
      border: 1px solid #e3e5dc;
      background: #fff;
    }
    .adjustment-value { min-width: 84px; text-align: center; font-weight: 700; }
    .adhan-stop-bar {
      position: fixed; z-index: 1000; inset-inline: 14px; bottom: max(14px, env(safe-area-inset-bottom));
      width: min(520px, calc(100% - 28px)); margin-inline: auto;
      padding: 10px; border: 1px solid rgba(255,255,255,.16);
      border-radius: 18px; background: var(--noor-forest-deep);
      box-shadow: 0 16px 44px rgba(0,0,0,.25);
    }
    .adhan-stop-bar button {
      width: 100%; min-height: 48px; border: 0; border-radius: 12px;
      background: #f5e8ce; color: #2e281c; font-weight: 800;
    }
    .dark {
      --noor-paper: #101b17;
      --noor-surface: #192720;
      --noor-ink: #f0eee5;
      --noor-muted: #b2beb5;
      --noor-line: #34453b;
      --noor-shadow: 0 12px 34px rgba(0,0,0,.16);
      color-scheme: dark;
    }
    .dark body { background: var(--noor-paper); color: var(--noor-ink); }
    .dark .settings-group, .dark .info-card, .dark .prayer-adjustment {
      background: var(--noor-surface); border-color: var(--noor-line);
    }
    .dark .settings-group .setting { border-color: rgba(210,220,210,.1); }
    .dark .settings-group .setting small { color: var(--noor-muted); }
    .dark .settings-group .select, .dark .settings-group select,
    .dark .prayer-adjustment-controls button {
      background: #202f27; color: var(--noor-ink); border-color: #43574a;
    }
    @media (max-width: 650px) {
      .main { width: min(100% - 20px, 1080px); }
      .settings-group { padding: 14px; border-radius: 18px; }
      .settings-list.settings-hub { gap: 10px; }
      .prayer-adjustment { padding: 10px; }
      .prayer-adjustment-controls { flex-wrap: wrap; }
    }
    @media (prefers-reduced-motion: reduce) {
      html { scroll-behavior: auto; }
      *, *::before, *::after { animation-duration: .01ms !important; animation-iteration-count: 1 !important; transition-duration: .01ms !important; }
    }
  `;
  document.head.append(style);
})();
