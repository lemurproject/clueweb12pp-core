'''
Core IP.Board forum code
'''

import re
import urlparse


def process_unfriendly_subforum_urls(url):
	'''
	The IP.Board subforums are of the type:
	foo.com/forum?showforum=42
	Args:
		- url :  a url containing the showforum key
	Returns:
		- url with only the subforum link
	'''

	url_parts = list(urlparse.urlparse(url))
	query_part = re.search(r'(showforum=\d+)', url_parts[4]).group(1)

	url_parts[4] = query_part

	return urlparse.urlunparse(url_parts)

def process_friendly_urls_subforum_windows(url):
	'''
	IP.Board forums on a windows machine look present an index
	page url that looks like:
	www.board.com/forums/index.php?/forum/10/my-test-forum

	Args:
		- url : a url with the format above
	Returns:
		- a url with only the subforum link in the above format
	'''
	return re.search(r'(.*index.php\?/forum/.*/).*', url).group(1)

def process_friendly_urls_subforum_apache(url):
	'''
	IP.Board forums on a apache web-server machine look present an index
	page url that looks like:
	www.board.com/forums/index.php/topic/99/my-test-topic

	Args:
		- url : a url with the format above
	Returns:
		- just the subforum link
	'''
	return re.search(r'(.*index.php/forum/.*/).*', url).group(1)

def process_friendly_urls_subforum_mod_rewrite(url):
	'''
	IP.Board forums on a apache web-server machine with
	mod-rewrite enabled look present an index
	page url that looks like:
	www.board.com/forums/index.php/topic/99/my-test-topic

	Args:
		- url : a url with the format above
	Returns:
		- just the subforum link

	'''
	return re.search(r'(.*/forum/.*/).*', url).group(1)

def process_unfriendly_topic_urls(url):
	'''
	Strip out fragments and just keep the topic id in there
	'''
	url_parts = list(urlparse.urlparse(url))
	query_part = url_parts[4]

	try:
		query_part = re.search(r'.*(showtopic=\d+).*', query_part).group(1)
		url_parts[4] = query_part
		return urlparse.urlunparse(url_parts)
	except:
		pass

def process_mod_rewrite_topic_urls(url):
	'''
	Strip out fragments and just keep the topic id in there
	'''
	url_match = re.search(r'(.*/topic/\d+.*/).*/?', url)
	try:
		return url_match.group(1).replace('unread', '')
	except:
		pass