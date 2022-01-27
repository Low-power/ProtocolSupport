package protocolsupport.protocol.utils.i18n;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import protocolsupport.utils.Utils;

public class I18NData {

	public static final String DEFAULT_LANG = "en_us";

	private static final HashMap<String, I18N> i18ns = new HashMap<>();
	private static final I18N defaulti18n = loadLocale(DEFAULT_LANG);

	public static void init() {
	}

	private static I18N loadLocale(String lang) {
		I18N i18n = new I18N(lang);
		BufferedReader reader = new BufferedReader(new InputStreamReader(Utils.getResource("i18n/" + lang + ".lang")));
		ArrayList<String> lines = new ArrayList<>();
		String line;
		try {
			while((line = reader.readLine()) != null) lines.add(line);
		} catch(IOException e) {
			e.printStackTrace();
		}
		i18n.load(lines);
		return i18n;
	}

	public static String i18n(String lang, String key, Object... args) {
		I18N i18n = i18ns.get(lang);
		if(i18n == null) i18n = defaulti18n;
		return String.format(i18n.getI18N(key), args);
	}

}
