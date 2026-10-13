package pe.pisacoma.iglesia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.pisacoma.iglesia.R
import pe.pisacoma.iglesia.ui.theme.Fraunces
import pe.pisacoma.iglesia.ui.theme.Tokens

/**
 * The cream search box (`.search-wrap` + `.search` on himnario/buscar): 14 dp radius,
 * parchment fill, sage glyph, Fraunces 500 input with an italic placeholder.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTag: String = "search-field",
    onSearch: () -> Unit = {},
    onClear: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xF0FBF9EF)) // rgba(251,249,239,.94)
            .border(1.dp, Tokens.sageA(0.20f), shape)
            .padding(start = 18.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(R.drawable.ic_nav_search), null, tint = Tokens.sageSoft, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    fontFamily = Fraunces,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.8.sp,
                    color = Color(0xFF9AA792),
                    modifier = Modifier.padding(vertical = 14.dp),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 16.8.sp, color = Tokens.inkDeep),
                cursorBrush = SolidColor(Tokens.sage),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).testTag(testTag),
            )
        }
        if (onClear != null && value.isNotEmpty()) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).pressable(onClick = onClear).testTag("$testTag-clear"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_nav_close), "Limpiar", tint = Tokens.sageText, modifier = Modifier.size(16.dp))
            }
        }
    }
}
