package com.example.howyoulookbald

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [takePicBtn.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [takePicBtn.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class takePicBtn : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_take_pic_btn, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inflate the layout for this fragment
        this.btnnTakePic.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                var v =context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(500)
            }
        })
    }

}
