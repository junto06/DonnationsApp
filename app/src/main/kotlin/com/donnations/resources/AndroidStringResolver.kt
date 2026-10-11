package com.donnations.resources

import android.content.Context
import com.donnations.core.base.StringResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidStringResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) : StringResolver {
    override fun getString(id: Int, vararg formatArgs: Any): String = context.getString(id, *formatArgs)

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String =
        context.resources.getQuantityString(id, quantity, *formatArgs)
}
