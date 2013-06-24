'''
Program to process the list of nabble posts and then produce a list of forum
init URLs
'''

import re


def get_nabble_base_topic_link(link):
	parts = re.match(r'(.*-td\d+)(i\d+)?.html', link)

	return '%(base_link)s.html' % {'base_link' : parts.group(1)}

def topic_url_before_redirect(link):
	parts = re.match(r'(.*)-td(\d+.html)', link)
	return parts.group(1) + '-tp' + parts.group(2)