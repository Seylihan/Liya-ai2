# Liya AI Android starter

Bu Android Studio projesi Liya'nın ilk çalışan prototipi için kaynak kodudur.

## İlk sürümde bulunanlar
- Jetpack Compose sohbet arayüzü
- Room ile telefonda kalıcı sohbet ve hafıza veritabanı
- Android Keystore tabanlı şifreli ayarlarda API anahtarı saklama
- OpenAI uyumlu Chat Completions API bağlantısı
- Android konuşmayı yazıya çevirme ve metni seslendirme
- `Liya, öğren: ...` / `Öğren: ...` komutlarıyla hafızaya bilgi kaydetme

## Derleme
1. Android Studio ile klasörü aç.
2. JDK 17 ve Android SDK 35 kurulu olsun.
3. Gradle senkronizasyonunu tamamla.
4. Build > Build Bundle(s) / APK(s) > Build APK(s) seç.

Ayarlar ekranına API anahtarını, sağlayıcının Chat Completions URL'sini ve model adını gir.

## Dürüst teknik notlar
- Bu prototipte hibrit yapı, çevrim içi AI + cihazda tutulan yerel hafıza anlamındadır. Telefonda çalışan bağımsız yerel dil modeli henüz eklenmedi.
- API anahtarını mobil uygulamada saklamak tam güvenlik sağlamaz. Gerçek dağıtım için bir sunucu proxy'si daha güvenlidir.
- Ses tanıma Android'in konuşma tanıma servisine bağlıdır; cihaz ve dil paketine göre internet isteyebilir.
- Bu kaynak paketi APK değildir; Android Studio ile derlenmesi gerekir. Burada Android SDK bulunmadığından APK derlemesi doğrulanmadı.
