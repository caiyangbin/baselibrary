package com.jplus.manyfunction.ui.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.jplus.manyfunction.R
import com.jplus.manyfunction.adapter.DownloadAdapter
import com.jplus.manyfunction.contract.DownloadListContract
import com.nice.baselibrary.base.adapter.BaseAdapter
import com.nice.baselibrary.base.common.Constant
import com.nice.baselibrary.base.download.DownloadState
import com.jplus.manyfunction.download.DownloadCallback
import com.nice.baselibrary.base.entity.vo.DownloadInfo
import com.nice.baselibrary.base.ui.BaseFragment
import com.nice.baselibrary.base.utils.StringUtils
import com.nice.baselibrary.base.utils.showGravityToast
import com.nice.baselibrary.widget.BaseCircleProgress
import com.nice.baselibrary.widget.JTextView
import com.nice.baselibrary.widget.dialog.BaseAlertDialog
import kotlinx.android.synthetic.main.fragemnt_download.*
import okhttp3.ResponseBody
import java.io.File


/**
 * @author JPlus
 * @date 2019/2/16.
 */
class DownloadListFragment : BaseFragment(), DownloadListContract.View {

    private var mPresenter: DownloadListContract.Presenter? = null
    private var mDownloadRecy: RecyclerView? = null
    private var mDownloadAdapter: DownloadAdapter? = null
    private var mShowDelete = false
    private var mCheckItems: ArrayList<DownloadInfo> = arrayListOf()

    override fun getInitView(view: View?, bundle: Bundle?) {
        mDownloadRecy = view?.findViewById(R.id.rcy_downloads)
        val rvManager = LinearLayoutManager(this.context)
        rvManager.stackFromEnd = true
        rvManager.reverseLayout =true
        mDownloadRecy?.layoutManager = rvManager
        mDownloadRecy?.itemAnimator = DefaultItemAnimator()
    }

    override fun bindListener() {
        val urls = "http://eyepetizer-test.oss-cn-beijing.aliyuncs.com/files/eyepetizer/5.3.482/eyepetizer-eyepetizer_web.apk"

        fab_input_url.setOnClickListener {
            this.activity?.let {
                BaseAlertDialog.Builder(it.supportFragmentManager)
                        .setLayoutRes(R.layout.view_input_url_dialog)
                        .setCancelable(true)
                        .setTag("newDialog")
                        .setScreenHeightPercent(0.5f)
                        .setScreenWidthPercent(0.8f)
                        .setAnimationRes(R.style.NiceDialogAnim)
                        .setGravity(Gravity.CENTER)
                        .setDimAmount(0.0f)
                        .addClickedId(R.id.btn_input_url)
                        .setBindViewListener(object : BaseAlertDialog.OnBindViewListener {
                            override fun onBindView(viewHolder: BaseAdapter.VH) {
                                viewHolder.getView<TextInputEditText>(R.id.input_edit_input_url).setText(urls)
                            }
                        })
                        .setViewClickListener(object : BaseAlertDialog.OnViewClickListener {
                            override fun onClick(viewHolder: BaseAdapter.VH, view: View, dialog: BaseAlertDialog) {
                                when (view.id) {
                                    R.id.btn_input_url -> {
                                        val url = viewHolder.getView<TextInputEditText>(R.id.input_edit_input_url).text.toString()
                                        mPresenter?.addDownload(url, File(Constant.Path.ROOT_DIR, Constant.Path.DOWNLOAD_DIR).absolutePath)
                                    }
                                }
                            }

                        }).create().show()
            }
        }

        btn_delete_cancel.setOnClickListener {
            fab_input_url.visibility = View.VISIBLE
            ly_delete_all.visibility = View.GONE
            mDownloadAdapter?.showCheck(false)
            mCheckItems.clear()
        }
        btn_delete_all.setOnClickListener {
            fab_input_url.visibility = View.VISIBLE
            ly_delete_all.visibility = View.GONE
            mDownloadAdapter?.showCheck(false)
            mDownloadAdapter?.deleteItems(mCheckItems)
            mPresenter?.removeDownloads(mCheckItems)
            mCheckItems.clear()
        }
    }

    override fun downloadIsExist(message: String) {
        activity?.showGravityToast(message)
    }


    override fun addDownload(item: DownloadInfo) {
        activity?.runOnUiThread {
            mDownloadAdapter?.addItem(item)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragemnt_download
    }

    override fun setPresenter(presenter: DownloadListContract.Presenter) {
        mPresenter = presenter
    }

    override fun showEmptyList() {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter?.subscribe()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.unSubscribe()
    }

    override fun showData(items: MutableList<DownloadInfo>) {
        Log.d("pipa", "showData====${items}")
        mDownloadAdapter = DownloadAdapter(items, object : DownloadAdapter.ItemClickListener {
            override fun setItemCheck(itemView: DownloadAdapter.VH, item: DownloadInfo, position: Int, checked: Boolean) {
                if (checked) {
                    Log.d("pipa", "position:$position, checked:$checked")
                    mCheckItems.add(item)
                } else {
                    if (item in mCheckItems) {
                        mCheckItems.remove(item)
                    }
                }
            }

            override fun setItemClick(itemView: DownloadAdapter.VH, item: DownloadInfo, position: Int) {
                Log.d("pipa", "setItemClick:info:$item")
                mPresenter?.controlDownload(item, setBindView(itemView))

            }

            @SuppressLint("RestrictedApi")
            override fun setItemLongClick(itemView: DownloadAdapter.VH, item: DownloadInfo, position: Int): Boolean {
                if (mShowDelete) {
                    mCheckItems.clear()
                    fab_input_url.visibility = View.VISIBLE
                    ly_delete_all.visibility = View.GONE
                    mDownloadAdapter?.showCheck(false)
                } else {
                    Log.d("pipa", "showCheck")
                    fab_input_url.visibility = View.GONE
                    ly_delete_all.visibility = View.VISIBLE
                    mDownloadAdapter?.showCheck(true)
                }
                mShowDelete = !mShowDelete
                return true
            }
        })
        mDownloadAdapter?.setItemBindListener(object : DownloadAdapter.ItemBindListener {
            override fun onBindListener(itemView: DownloadAdapter.VH, item: DownloadInfo, position: Int): Boolean {
                if (item.status == DownloadState.DOWNLOAD_ING) {
                    mPresenter?.reBindListener(item, setBindView(itemView))
                    return false
                }
                return true
            }
        })
        mDownloadRecy?.adapter = mDownloadAdapter
    }

    private fun setBindView(itemView: DownloadAdapter.VH): DownloadCallback {
        return object : DownloadCallback {
            override fun next(responseBody: ResponseBody) {

            }

            override fun update(read: Long, count: Long, done: Boolean) {
                activity?.runOnUiThread {
                    itemView.getView<BaseCircleProgress>(R.id.bcp_download_item).loading(String.format("%.1f", read * 100.0 / count).toDouble())
                    itemView.getView<JTextView>(R.id.btv_download_item_ratio).text = "${StringUtils.parseByteSize(read)}/${StringUtils.parseByteSize(count)}"
                }
            }

            override fun downloadSuccess() {
                activity?.runOnUiThread {
                    itemView.getView<BaseCircleProgress>(R.id.bcp_download_item).success()
                }
            }

            override fun downloadFailed(e: Throwable) {
                Log.e("pipa", e.message)
                activity?.runOnUiThread {
                    itemView.getView<BaseCircleProgress>(R.id.bcp_download_item).failed()
                }
            }

            override fun pause() {

            }

            override fun downloadCancel() {
                activity?.runOnUiThread {
                    itemView.getView<BaseCircleProgress>(R.id.bcp_download_item).cancel()
                }
            }
        }
    }

    override fun addDownloads(downloads: MutableList<DownloadInfo>) {

    }

    override fun removeDownloads(items: MutableList<DownloadInfo>) {
        mDownloadAdapter?.deleteItems(items)
    }
}