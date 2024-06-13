package com.eylem.yemekkitabi.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.eylem.yemekkitabi.databinding.FragmentTarifBinding
import com.eylem.yemekkitabi.model.Tarif
import com.eylem.yemekkitabi.roomdb.TarifDAO
import com.eylem.yemekkitabi.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel: Uri? = null
    private var secilenBitmap: Bitmap? = null
    private val mDisposable = CompositeDisposable()
    private var secilenTarif : Tarif? = null

    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
        tarifDao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener{gorselSec(it)}
        binding.kaydetbutton.setOnClickListener { kaydet(it) }
        binding.silbutton.setOnClickListener { sil(it) }
        arguments?.let {
            var bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (bilgi == "yeni"){
                // yeni tarif eklenecek o yüzden sil butonu deaktif kaydet aktif
                secilenTarif = null
                binding.silbutton.isEnabled = false
                binding.kaydetbutton.isEnabled = true
                binding.isimText.setText("")
                binding.malzemeText.setText("")
                binding.yapilisText.setText("")
            }
            else{
                // eski tarif görüntülenecek o yüzden sil butonu aktif kaydet deaktif
                binding.silbutton.isEnabled = true
                binding.kaydetbutton.isEnabled = false
                val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )

            }
        }

    }
    private fun  handleResponse(tarif: Tarif){
        val bitmap = tarif.gorsel?.let { BitmapFactory.decodeByteArray(tarif.gorsel,0, it.size) }
        binding.imageView.setImageBitmap(bitmap)
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        binding.yapilisText.setText(tarif.yapilis)

        secilenTarif = tarif
    }

    fun gorselSec(view: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // önceden bu izin verilmiş mi kontrol ediyor. Eğer verilmemişse if bloğu içinde işlem yapılıyor
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // eğer izin daha önce reddedildi ise bu izni tekrar istemek için
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)) {
                    // snackbar göstermemiz gerekiyor. Burada kullanıcıya neden izin istediğimizi bir kere daha belirtiyoruz.
                    Snackbar.make(view,"Görsel Seçmek İçin Galeriye Erişim İzni Vermeniz Gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            // izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }
                else{
                    // izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // izin verilmiş, galeriye gideceğiz
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else {
            // önceden bu izin verilmiş mi kontrol ediyor. Eğer verilmemişse if bloğu içinde işlem yapılıyor
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // eğer izin daha önce reddedildi ise bu izni tekrar istemek için
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // snackbar göstermemiz gerekiyor. Burada kullanıcıya neden izin istediğimizi bir kere daha belirtiyoruz.
                    Snackbar.make(view,"Görsel Seçmek İçin Galeriye Erişim İzni Vermeniz Gerekiyor",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            // izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                }
                else{
                    // izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                // izin verilmiş, galeriye gideceğiz
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }


    }
    fun kaydet(view: View){
        val isim = binding.isimText.text.toString()
        val malzeme = binding.malzemeText.text.toString()
        val yapilis = binding.yapilisText.text.toString()
        if (secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()
            val tarif = Tarif(isim,malzeme,yapilis,byteDizisi)


            //RxJava
            mDisposable.add(
                tarifDao.insert(tarif).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers
                .mainThread()).subscribe(this::handleResponseForInsert))

        }

    }

    private fun handleResponseForInsert(){
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    fun sil(view: View) {
        if (secilenTarif != null){
            mDisposable.add(
                tarifDao.delete(tarif = secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }


    }
    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                   secilenGorsel = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                secilenGorsel!!
                            )
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                secilenGorsel
                            )
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: Exception){
                        println(e.localizedMessage)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                //permission granted
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(requireContext(), "Permisson needed!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
    private fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int) : Bitmap {
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height
        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if(bitmapOrani >= 1){
            // görsel yatay
            width = maximumBoyut
            val kisaltilmisYukseklik = width / bitmapOrani
            height = kisaltilmisYukseklik.toInt()
        }else{
            // görsel dikey
            height = maximumBoyut
            val kisaltilmisGenislik = height * bitmapOrani
            width = kisaltilmisGenislik.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }


}