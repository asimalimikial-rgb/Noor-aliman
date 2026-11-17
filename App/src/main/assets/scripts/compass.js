
(()=>{
  const status=document.getElementById('compassStatus');
  const permissionButton=document.getElementById('compassPermission');
  const cameraOverlay=document.getElementById('qiblaCamera');
  const cameraVideo=document.getElementById('qiblaCameraVideo');
  const cameraStatus=document.getElementById('arCameraStatus');
  let smoothHeading=NaN;
  let browserHeadingSeen=false;
  let cameraStream=null;
  const normalize=value=>((value%360)+360)%360;
  const setStatus=text=>{if(status)status.textContent=text};
  const setCameraStatus=text=>{if(cameraStatus)cameraStatus.textContent=text};
  function updateArDirection(){
    const bearing=Number(window.qiblaBearing);
    if(!Number.isFinite(bearing))return;
    const heading=Number(window.qiblaHeading);
    const arrow=document.getElementById('cameraArrow');
    const degrees=document.getElementById('arBearing');
    if(arrow)arrow.style.transform='rotate('+(bearing-(Number.isFinite(heading)?heading:0)).toFixed(1)+'deg)';
    if(degrees){const display=document.getElementById('qDeg');degrees.textContent=display?display.textContent:''}
  }
  window.updateArQiblaDirection=updateArDirection;
  function stopCamera(){
    if(cameraStream){cameraStream.getTracks().forEach(track=>track.stop());cameraStream=null}
    if(cameraVideo){try{cameraVideo.pause()}catch(error){}cameraVideo.srcObject=null}
    if(cameraOverlay){cameraOverlay.classList.remove('open');cameraOverlay.setAttribute('aria-hidden','true')}
    document.body.classList.remove('camera-open');
  }
  window.stopQiblaCamera=stopCamera;
  async function openCamera(){
    if(!cameraOverlay)return;
    cameraOverlay.classList.add('open');cameraOverlay.setAttribute('aria-hidden','false');
    document.body.classList.add('camera-open');
    setCameraStatus('جارٍ طلب إذن الكاميرا...');
    updateArDirection();
    if(window.refreshQiblaCompass)window.refreshQiblaCompass();
    if(!navigator.mediaDevices||!navigator.mediaDevices.getUserMedia){
      setCameraStatus('الكاميرا غير متاحة هنا؛ استخدم البوصلة الظاهرة في الصفحة.');
      return;
    }
    try{
      const stream=await navigator.mediaDevices.getUserMedia({video:{facingMode:{ideal:'environment'}},audio:false});
      if(!cameraOverlay.classList.contains('open')){stream.getTracks().forEach(track=>track.stop());return}
      cameraStream=stream;cameraVideo.srcObject=stream;
      try{await cameraVideo.play()}catch(error){}
      setCameraStatus('الكاميرا تعمل. حرّك الهاتف ببطء واتبع السهم.');
    }catch(error){
      setCameraStatus(error&&error.name==='NotAllowedError'?'لم يتم السماح بالكاميرا. أغلق العرض أو غيّر الإذن من إعدادات الجهاز.':'تعذّر فتح الكاميرا على هذا الجهاز.');
    }
  }
  const cameraButton=document.getElementById('openQiblaCamera');
  const closeCameraButton=document.getElementById('closeQiblaCamera');
  if(cameraButton)cameraButton.addEventListener('click',openCamera);
  if(closeCameraButton)closeCameraButton.addEventListener('click',()=>{stopCamera();if(window.refreshQiblaCompass)window.refreshQiblaCompass()});
  window.addEventListener('keydown',event=>{if(event.key==='Escape'&&cameraOverlay&&cameraOverlay.classList.contains('open')){stopCamera();if(window.refreshQiblaCompass)window.refreshQiblaCompass()}});
  function updateHeading(value,source){
    if(window.isCompassEnabled&&!window.isCompassEnabled())return;
    if(!Number.isFinite(value))return;
    value=normalize(value);
    if(!Number.isFinite(smoothHeading))smoothHeading=value;
    else smoothHeading+=((((value-normalize(smoothHeading)+540)%360)-180)*0.22);
    window.qiblaHeading=smoothHeading;
    if(Number.isFinite(window.qiblaBearing)){
      const needle=document.getElementById('needle');
      if(needle)needle.style.transform='rotate('+(window.qiblaBearing-smoothHeading).toFixed(1)+'deg)';
    }
    updateArDirection();
    setStatus(source==='native'?'البوصلة مفعّلة':'البوصلة مفعّلة عبر مستشعر الجهاز');
  }
  window.resetQiblaHeading=function(){
    smoothHeading=NaN;
    window.qiblaHeading=NaN;
    const bearing=Number(window.qiblaBearing);
    const needle=document.getElementById('needle');
    if(needle&&Number.isFinite(bearing))needle.style.transform='rotate('+bearing.toFixed(1)+'deg)';
    updateArDirection();
  };
  function onDeviceOrientation(event){
    const page=document.getElementById('qibla');
    if(!page||!page.classList.contains('active')||(window.isCompassEnabled&&!window.isCompassEnabled()))return;
    let heading=Number.isFinite(event.webkitCompassHeading)?event.webkitCompassHeading:NaN;
    if(!Number.isFinite(heading)&&event.absolute===true&&Number.isFinite(event.alpha))heading=normalize(360-event.alpha);
    if(Number.isFinite(heading)){browserHeadingSeen=true;updateHeading(heading,'browser')}
  }
  window.addEventListener('deviceorientationabsolute',onDeviceOrientation,true);
  window.addEventListener('deviceorientation',onDeviceOrientation,true);
  window.refreshQiblaCompass=function(){
    const page=document.getElementById('qibla');
    if(!page||!page.classList.contains('active')){
      stopCamera();
      if(window.NativeCompass)window.NativeCompass.stop();
      return;
    }
    if(window.NativeCompass){
      const place=window.qiblaCoordinates;
      const started=!!place&&window.NativeCompass.start(place.lat,place.lng);
      if(started)setStatus('جارٍ قراءة اتجاه الهاتف...');
      else setStatus('تعذّر تشغيل بوصلة الهاتف؛ سيبقى الاتجاه محسوبًا من الشمال الحقيقي.');
    }else if(!browserHeadingSeen){
      setStatus('يُعرض اتجاه القبلة من الشمال الحقيقي؛ فعّل المستشعر لعرض اتجاه الهاتف.');
    }
    if(!window.NativeCompass&&typeof DeviceOrientationEvent!=='undefined'&&typeof DeviceOrientationEvent.requestPermission==='function')permissionButton.hidden=false;
  };
  if(permissionButton)permissionButton.addEventListener('click',async()=>{
    try{
      const result=await DeviceOrientationEvent.requestPermission();
      if(result==='granted'){permissionButton.hidden=true;setStatus('حرّك الهاتف لتفعيل مستشعر البوصلة.')}
      else setStatus('لم يُمنح إذن مستشعر الاتجاه.');
    }catch(error){setStatus('تعذّر تفعيل مستشعر الاتجاه.');}
  });
  setInterval(()=>{
    const page=document.getElementById('qibla');
    if(!window.NativeCompass||!page||!page.classList.contains('active'))return;
    const heading=Number(window.NativeCompass.headingDegrees());
    if(heading>=0)updateHeading(heading,'native');
  },120);
  setTimeout(()=>window.refreshQiblaCompass(),0);
  setTimeout(updateArDirection,0);
})();
