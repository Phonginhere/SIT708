package com.example.pass51_istream.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pass51_istream.R
import com.example.pass51_istream.database.AppDatabase
import com.example.pass51_istream.database.PlaylistItem
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getInt("userId", -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        // TODO: find all views
            val urlInput = view.findViewById<EditText>(R.id.etYoutubeUrl)
            val webView = view.findViewById<WebView>(R.id.youtubeView)
            val btnPlay = view.findViewById<Button>(R.id.btnPlay)
            val btnAddToPlaylist = view.findViewById<Button>(R.id.btnAddToPlaylist)
            val btnMyPlaylist = view.findViewById<Button>(R.id.btnMyPlaylist)
            val btnLogout = view.findViewById<Button>(R.id.btnLogout)


        // TODO: set up WebView settings
        // WebView settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.userAgentString = webView.settings.userAgentString.replace("wv", "")
        webView.webChromeClient = android.webkit.WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                return false
            }
        }
        webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        // TODO: set up all four button click listeners, refer from fragment_home.xml, logout, add to playlist, my playlist
            btnLogout.setOnClickListener {
                Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, LoginFragment())
                    .commit()
            }
        btnPlay.setOnClickListener {
            val url = urlInput.text.toString()
            val videoId = extractVideoId(url)
            if (videoId == null) {
                Toast.makeText(requireContext(), "Invalid YouTube URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            webView.visibility = View.VISIBLE
            val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * { margin: 0; padding: 0; }
                html, body { width: 100%; height: 100%; background: #000; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <iframe src="https://www.youtube.com/embed/$videoId?playsinline=1&autoplay=1&rel=0&origin=https://example.com"
                allowfullscreen allow="autoplay; encrypted-media">
            </iframe>
        </body>
        </html>
    """
            webView.loadDataWithBaseURL(
                "https://example.com",
                html,
                "text/html",
                "utf-8",
                null
            )
        }
            btnAddToPlaylist.setOnClickListener {
                val url = urlInput.text.toString()
                val videoId = extractVideoId(url)
                if (videoId == null) {
                    Toast.makeText(requireContext(), "Invalid YouTube URL", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    db.playlistDao().insertPlaylistItem(PlaylistItem(url = url, userId = userId))
                    Toast.makeText(requireContext(), "Video added to playlist!", Toast.LENGTH_SHORT).show()
                }
            }
        btnMyPlaylist.setOnClickListener {
            val playlistFragment = PlaylistFragment()
            val bundle = Bundle()
            bundle.putInt("userId", userId)
            playlistFragment.arguments = bundle
            playlistFragment.onPlaylistItemClick = { url ->
                // Navigate back to HomeFragment with the URL
                val homeFragment = HomeFragment()
                val homeBundle = Bundle()
                homeBundle.putInt("userId", userId)
                homeBundle.putString("videoUrl", url)
                homeFragment.arguments = homeBundle
                parentFragmentManager.popBackStack()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, homeFragment)
                    .commit()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, playlistFragment)
                .addToBackStack(null)
                .commit()
        }

        val passedUrl = arguments?.getString("videoUrl")
        if (passedUrl != null) {
            urlInput.setText(passedUrl)
            val videoId = extractVideoId(passedUrl)
            if (videoId != null) {
                webView.visibility = View.VISIBLE
                val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; }
                    html, body { width: 100%; height: 100%; background: #000; }
                    iframe { width: 100%; height: 100%; border: none; }
                </style>
            </head>
            <body>
                <iframe src="https://www.youtube.com/embed/$videoId?playsinline=1&autoplay=1&rel=0&origin=https://example.com"
                    allowfullscreen allow="autoplay; encrypted-media">
                </iframe>
            </body>
            </html>
        """
                webView.loadDataWithBaseURL(
                    "https://example.com",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        }
    }

    // Helper: extract video ID from YouTube URL
    private fun extractVideoId(url: String): String? {
        val patterns = listOf(
            "v=([a-zA-Z0-9_-]{11})",
            "youtu\\.be/([a-zA-Z0-9_-]{11})",
            "embed/([a-zA-Z0-9_-]{11})"
        )
        for (pattern in patterns) {
            val matcher = Regex(pattern).find(url)
            if (matcher != null) return matcher.groupValues[1]
        }
        return null
    }
}