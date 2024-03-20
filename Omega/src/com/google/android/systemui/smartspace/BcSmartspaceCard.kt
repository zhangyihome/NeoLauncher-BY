package com.google.android.systemui.smartspace

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.android.launcher3.R
import com.saulhdev.smartspace.SmartspaceAction
import com.saulhdev.smartspace.SmartspaceTarget
import com.saulhdev.smartspace.hasIntent
import java.util.Locale
import java.util.UUID

class BcSmartspaceCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var baseActionIconSubtitleView: DoubleShadowTextView? = null
    private var dateView: IcuDateTextView? = null
    private var dndImageView: ImageView? = null
    private var extrasGroup: ViewGroup? = null
    private var iconDrawable: DoubleShadowIconDrawable? = null
    private var iconTintColor = 0
    private var nextAlarmImageView: ImageView? = null
    private var nextAlarmTextView: TextView? = null
    private var subtitleTextView: TextView? = null
    lateinit var target: SmartspaceTarget
    private var titleTextView: TextView? = null
    private var topPadding = 0
    private var usePageIndicatorUi = false
    private var textGroup: ViewGroup? = null
    private var secondaryCardGroup: ViewGroup? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        textGroup = findViewById(R.id.text_group);
        secondaryCardGroup = findViewById(R.id.secondary_card_group);
        dateView = findViewById(R.id.date)
        titleTextView = findViewById(R.id.title_text)
        subtitleTextView = findViewById(R.id.subtitle_text)
        baseActionIconSubtitleView = findViewById(R.id.base_action_icon_subtitle)
        extrasGroup = findViewById(R.id.smartspace_extras_group)
        topPadding = paddingTop
        extrasGroup?.let {
            dndImageView = it.findViewById(R.id.dnd_icon)
            nextAlarmImageView = it.findViewById(R.id.alarm_icon)
            nextAlarmTextView = it.findViewById(R.id.alarm_text)
        }
    }

    fun setSmartspaceTarget(target: SmartspaceTarget, multipleCards: Boolean) {
        this.target = target
        val headerAction = target.headerAction
        val baseAction = target.baseAction
        usePageIndicatorUi = multipleCards

        if (headerAction != null) {
            iconDrawable = BcSmartSpaceUtil.getIconDrawable(context, headerAction.icon)
                ?.let { DoubleShadowIconDrawable(it, context) }

            var title: CharSequence? = headerAction.title
            var subtitle = headerAction.subtitle
            val hasTitle =
                target.featureType == SmartspaceTarget.FEATURE_WEATHER || !TextUtils.isEmpty(
                    title
                )
            val hasSubtitle = !TextUtils.isEmpty(subtitle)
            if (!hasTitle) {
                title = subtitle
            }
            val contentDescription = headerAction.contentDescription
            setTitle(title, contentDescription, hasTitle != hasSubtitle)
            if (!hasTitle || !hasSubtitle) {
                subtitle = null
            }
            setSubtitle(subtitle, headerAction.contentDescription)
            updateIconTint()
        }

        if (baseAction != null && baseActionIconSubtitleView != null) {
            val icon = BcSmartSpaceUtil.getIconDrawable(context, baseAction.icon)
                ?.let { DoubleShadowIconDrawable(it, context) }
            val iconView = baseActionIconSubtitleView!!
            if (icon != null) {
                icon.setTintList(null)
                iconView.text = baseAction.subtitle
                iconView.setCompoundDrawablesRelative(icon, null, null, null)
                iconView.isVisible = true
                BcSmartSpaceUtil.setOnClickListener(iconView, baseAction, null, "BcSmartspaceCard")
                setFormattedContentDescription(
                    iconView,
                    baseAction.subtitle,
                    baseAction.contentDescription
                )
            } else {
                iconView.isInvisible = true
                iconView.setOnClickListener(null)
                iconView.contentDescription = null
            }
        }

        dateView?.let {
            val calendarAction = SmartspaceAction(
                id = headerAction?.id ?: baseAction?.id ?: UUID.randomUUID().toString(),
                title = "unusedTitle",
                intent = BcSmartSpaceUtil.getOpenCalendarIntent()
            )
            BcSmartSpaceUtil.setOnClickListener(it, calendarAction, null, "BcSmartspaceCard")
        }

        when {
            headerAction.hasIntent -> {
                BcSmartSpaceUtil.setOnClickListener(this, headerAction, null, "BcSmartspaceCard")
            }

            baseAction.hasIntent -> {
                BcSmartSpaceUtil.setOnClickListener(this, baseAction, null, "BcSmartspaceCard")
            }

            else -> {
                BcSmartSpaceUtil.setOnClickListener(this, headerAction, null, "BcSmartspaceCard")
            }
        }
    }

    fun setPrimaryTextColor(textColor: Int) {
        titleTextView?.setTextColor(textColor)
        dateView?.setTextColor(textColor)
        subtitleTextView?.setTextColor(textColor)
        baseActionIconSubtitleView?.setTextColor(textColor)
        iconTintColor = textColor
        updateIconTint()
    }

    fun setTitle(title: CharSequence?, contentDescription: CharSequence?, hasIcon: Boolean) {
        val titleView = titleTextView ?: return
        val isRTL =
            TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
        titleView.textAlignment = if (isRTL) TEXT_ALIGNMENT_TEXT_END else TEXT_ALIGNMENT_TEXT_START
        titleView.text = title
        titleView.setCompoundDrawablesRelative(
            if (hasIcon) iconDrawable else null, null,
            null, null
        )
        if (target.featureType == SmartspaceTarget.FEATURE_CALENDAR
            && Locale.ENGLISH.language == context.resources.configuration.locale.language
        ) {
            titleView.ellipsize = TextUtils.TruncateAt.MIDDLE
        } else {
            titleView.ellipsize = TextUtils.TruncateAt.END
        }
        if (hasIcon) {
            setFormattedContentDescription(titleView, title, contentDescription)
        }
    }

    private fun setSubtitle(subtitle: CharSequence?, charSequence2: CharSequence?) {
        val subtitleView = subtitleTextView ?: return
        subtitleView.text = subtitle
        subtitleTextView!!.setCompoundDrawablesRelative(
            if (TextUtils.isEmpty(subtitle)) null else iconDrawable, null,
            null, null
        )
        subtitleTextView!!.maxLines =
            if (target.featureType == SmartspaceTarget.FEATURE_TIPS && !usePageIndicatorUi) 2 else 1
        setFormattedContentDescription(subtitleTextView!!, subtitle, charSequence2)
    }

    private fun setFormattedContentDescription(
        textView: TextView,
        title: CharSequence?,
        contentDescription: CharSequence?
    ) {
        var description: CharSequence? = title
        if (TextUtils.isEmpty(description)) {
            description = contentDescription
        } else if (!TextUtils.isEmpty(contentDescription)) {
            description = context.getString(
                R.string.generic_smartspace_concatenated_desc,
                contentDescription,
                description
            )
        }
        textView.contentDescription = description
    }

    private fun updateIconTint() {
        val icon = iconDrawable ?: return
        when (target.featureType) {
            SmartspaceTarget.FEATURE_WEATHER -> icon.setTintList(null)
            else -> icon.setTint(iconTintColor)
        }
    }
}
