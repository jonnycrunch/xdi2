package xdi2.transport.spring;

import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import xdi2.core.syntax.XDIAddress;
import xdi2.messaging.container.contributor.Contributor;
import xdi2.messaging.container.contributor.ContributorMap;

public class MapContributorMapConverter implements Converter<Map<?, ?>, ContributorMap> {

	@Override
	public ContributorMap convert(Map<?, ?> source) {

		ContributorMap target = new ContributorMap();

		for (Map.Entry<?, ?> item : source.entrySet()) {

			Object key = item.getKey();

			if (key instanceof String) key = XDIAddress.create((String) key);
			
			Object value = item.getValue();

			if (value instanceof Contributor) {

				target.addContributor((XDIAddress) key, (Contributor) value);
			} else if (value instanceof List<?>) {

				for (Object item2 : (List<?>) value) {

					target.addContributor((XDIAddress) key, (Contributor) item2);
				}
			}
		}

		return target;
	}
}
