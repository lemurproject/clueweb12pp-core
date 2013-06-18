'''
vBulletin core
'''

import re
import urlparse


def process_unfriendly_subforum_urls(url):
	'''
	The vBulletin subforums are of the type:
	board.com/optional/forumdisplay.php?topic_name
	OR
	board.com/optional/forumdisplay.php?f=a
	Args:
		- url :  a url containing the forumdisplay key
	Returns:
		- url with only the subforum link
	'''

	parsed = urlparse.urlparse(url)

	import ipdb
	ipdb.set_trace()